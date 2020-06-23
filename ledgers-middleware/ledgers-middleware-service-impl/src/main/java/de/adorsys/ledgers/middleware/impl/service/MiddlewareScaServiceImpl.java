package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.payment.ConsentKeyDataTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentCoreDataTO;
import de.adorsys.ledgers.middleware.api.domain.sca.*;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareScaService;
import de.adorsys.ledgers.middleware.impl.converter.*;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCoreDataPolicy;
import de.adorsys.ledgers.sca.domain.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.AuthorizationService;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.ACTC;
import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.PATC;
import static de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO.SCAMETHODSELECTED;
import static de.adorsys.ledgers.sca.domain.OpTypeBO.CANCEL_PAYMENT;
import static de.adorsys.ledgers.sca.domain.OpTypeBO.PAYMENT;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("PMD.TooManyMethods")
public class MiddlewareScaServiceImpl implements MiddlewareScaService {

    private final UserMapper userMapper;
    private final UserService userService;
    private final AisConsentBOMapper aisConsentMapper;
    private final BearerTokenMapper bearerTokenMapper;
    private final SCAOperationService scaOperationService;
    private final SCAUtils scaUtils;
    private final AccessService accessService;
    private final ScaInfoMapper scaInfoMapper;
    private final AuthorizationService authorizationService;
    private final ScaResponseResolver scaResponseResolver;
    private final PaymentCoreDataPolicy coreDataPolicy;
    private final DepositAccountPaymentService paymentService;

    @Value("${default.token.lifetime.seconds:600}")
    private int defaultLoginTokenExpireInSeconds;

    @Value("${sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;


    // AIS


    /*
     * Starts the SCA process. Might directly produce the consent token if
     * sca is not needed.
     *
     * (non-Javadoc)
     * @see de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService#startScaAis(java.lang.String, de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO)
     */
    @Override
    public ScaResponse startScaAis(ScaInfoTO scaInfoTO, String consentId, AisConsentTO aisConsent) {
        BearerTokenBO bearerToken = checkAisConsent(scaInfoMapper.toScaInfoBO(scaInfoTO), aisConsent);
        ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsent);
        ScaResponse response = prepareSCA(scaInfoTO, scaUtils.userBO(scaInfoTO.getUserId()), aisConsent, consentKeyData);
        if (ScaStatusTO.EXEMPTED.equals(response.getScaStatus())) {
            response.setBearerToken(bearerTokenMapper.toBearerTokenTO(bearerToken));
        }
        return response;
    }

    @Override
    public ScaResponse getScaAis(String userId, String consentId, String authorisationId) {
        UserBO user = scaUtils.userBO(userId);
        AisConsentBO consent = userService.loadConsent(consentId);
        AisConsentTO aisConsentTO = aisConsentMapper.toAisConsentTO(consent);
        ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsentTO);
        SCAOperationBO scaOperationBO = scaUtils.loadAuthCode(authorisationId);
        int scaWeight = accessService.resolveMinimalScaWeightForConsent(consent.getAccess(), user.getAccountAccesses());
        ScaResponse response = toScaConsentResponse(userMapper.toUserTO(user), consent.getId(), consentKeyData.template(), scaOperationBO);
        response.setMultilevelScaRequired(multilevelScaEnable && scaWeight < 100);
        return response;
    }

    @Override
    public ScaResponse selectScaMethodAis(String userId, String consentId, String authorisationId, String scaMethodId) {
        UserBO userBO = scaUtils.userBO(userId);
        AisConsentBO consent = userService.loadConsent(consentId);
        AisConsentTO aisConsentTO = aisConsentMapper.toAisConsentTO(consent);
        ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsentTO);
        String template = consentKeyData.template();
        int scaWeight = accessService.resolveMinimalScaWeightForConsent(consent.getAccess(), userBO.getAccountAccesses());

        AuthCodeDataBO a = new AuthCodeDataBO(userBO.getLogin(), scaMethodId,
                                              consentId, template, template,
                                              defaultLoginTokenExpireInSeconds, OpTypeBO.CONSENT, authorisationId, scaWeight);

        SCAOperationBO scaOperationBO = scaOperationService.generateAuthCode(a, userBO, ScaStatusBO.SCAMETHODSELECTED);
        ScaResponse response = toScaConsentResponse(userMapper.toUserTO(userBO), consent.getId(), consentKeyData.template(), scaOperationBO);
        response.setMultilevelScaRequired(multilevelScaEnable && scaWeight < 100);
        return response;
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    @Transactional(noRollbackFor = ScaModuleException.class)
    public ScaResponse authorizeConsent(ScaInfoTO scaInfoTO, String consentId) {
        AisConsentBO consent = userService.loadConsent(consentId);
        AisConsentTO aisConsentTO = aisConsentMapper.toAisConsentTO(consent);
        ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsentTO);

        UserBO userBO = scaUtils.userBO(scaInfoTO.getUserId());
        int scaWeight = accessService.resolveMinimalScaWeightForConsent(consent.getAccess(), userBO.getAccountAccesses());
        ScaValidationBO scaValidationBO = scaOperationService.validateAuthCode(scaInfoTO.getAuthorisationId(), consentId,
                                                                               consentKeyData.template(), scaInfoTO.getAuthCode(), scaWeight);

        UserTO userTO = scaUtils.user(userBO);
        SCAOperationBO scaOperationBO = scaUtils.loadAuthCode(scaInfoTO.getAuthorisationId());
        ScaResponse response = toScaConsentResponse(userTO, consent.getId(), consentKeyData.template(), scaOperationBO);
        response.setAuthConfirmationCode(scaValidationBO.getAuthConfirmationCode());
        if (!scaOperationService.authenticationCompleted(consentId, OpTypeBO.CONSENT)) {
            response.setPartiallyAuthorised(multilevelScaEnable);
            response.setMultilevelScaRequired(multilevelScaEnable);
        }
        BearerTokenBO consentToken = authorizationService.consentToken(scaInfoMapper.toScaInfoBO(scaInfoTO), consent);
        response.setBearerToken(bearerTokenMapper.toBearerTokenTO(consentToken));
        return response;
    }


    private BearerTokenBO checkAisConsent(ScaInfoBO scaInfoBO, AisConsentTO aisConsent) {
        AisConsentBO consentBO = aisConsentMapper.toAisConsentBO(aisConsent);
        return authorizationService.consentToken(scaInfoBO, consentBO);

    }

    private ScaResponse prepareSCA(ScaInfoTO scaInfoTO, UserBO user, AisConsentTO aisConsent, ConsentKeyDataTO consentKeyData) {
        String consentKeyDataTemplate = consentKeyData.template();
        UserTO userTo = scaUtils.user(user);
        String authorisationId = scaUtils.authorisationId(scaInfoTO);
        if (!scaUtils.hasSCA(user)) {
            ScaResponse response = new ScaResponse();
            response.setAuthorisationType(AuthorisationType.AIS);
            response.setAuthorisationId(authorisationId);
            response.setParentId(aisConsent.getId());
            response.setPsuMessage(consentKeyData.exemptedTemplate());
            response.setScaStatus(ScaStatusTO.EXEMPTED);
            response.setStatusDate(LocalDateTime.now());
            return response;
        } else {
            // start SCA
            AisConsentBO consentBO = aisConsentMapper.toAisConsentBO(aisConsent);
            consentBO = userService.storeConsent(consentBO);

            int scaWeight = accessService.resolveMinimalScaWeightForConsent(consentBO.getAccess(), user.getAccountAccesses());

            AuthCodeDataBO authCodeData = new AuthCodeDataBO(user.getLogin(), null, aisConsent.getId(), consentKeyDataTemplate, consentKeyDataTemplate, defaultLoginTokenExpireInSeconds, OpTypeBO.CONSENT, authorisationId, scaWeight);
            // FPO no auto generation of SCA AutCode. Process shall always be triggered from outside
            // The system. Even if a user ha only one sca method.
            SCAOperationBO scaOperationBO = scaOperationService.createAuthCode(authCodeData, ScaStatusBO.PSUAUTHENTICATED);
            ScaResponse response = toScaConsentResponse(userTo, consentBO.getId(), consentKeyDataTemplate, scaOperationBO);
            response.setMultilevelScaRequired(multilevelScaEnable && scaWeight < 100);
            return response;
        }
    }

    private ScaResponse toScaConsentResponse(UserTO user, String consentId, String messageTemplate, SCAOperationBO operation) {
        ScaResponse response = new ScaResponse();
        response.setAuthorisationType(AuthorisationType.AIS);
        scaResponseResolver.completeResponse(response, operation, user, messageTemplate, null);
        response.setParentId(consentId);
        return response;
    }


    // PIS


    /*
     * Authorizes a payment. Schedule the payment for execution if no further authorization is required.
     *
     */
    @Override
    @Transactional(noRollbackFor = ScaModuleException.class)
    public SCAPaymentResponseTO authorizePayment(ScaInfoTO scaInfoTO, String paymentId) {
        return authorizeOperation(scaInfoTO, paymentId, null, PAYMENT);
    }

    @Override
    public SCAPaymentResponseTO loadSCAForPaymentData(ScaInfoTO scaInfoTO, String paymentId) {
        return loadSca(scaInfoTO, paymentId, scaInfoTO.getAuthorisationId(), PAYMENT);
    }

    @Override
    public SCAPaymentResponseTO loadSCAForCancelPaymentData(ScaInfoTO scaInfoTO, String paymentId, String cancellationId) {
        return loadSca(scaInfoTO, paymentId, cancellationId, CANCEL_PAYMENT);
    }

    @Override
    public SCAPaymentResponseTO selectSCAMethodForPayment(ScaInfoTO scaInfoTO, String paymentId) {
        return selectSCAMethod(scaInfoTO, paymentId, null, PAYMENT);
    }

    @Override
    public SCAPaymentResponseTO selectSCAMethodForCancelPayment(ScaInfoTO scaInfoTO, String paymentId, String cancellationId) {
        return selectSCAMethod(scaInfoTO, paymentId, cancellationId, CANCEL_PAYMENT);
    }

    @Override
    @Transactional(noRollbackFor = ScaModuleException.class)
    public SCAPaymentResponseTO authorizeCancelPayment(ScaInfoTO scaInfoTO, String paymentId, String cancellationId) {
        return authorizeOperation(scaInfoTO, paymentId, cancellationId, CANCEL_PAYMENT);
    }

    private SCAPaymentResponseTO selectSCAMethod(ScaInfoTO scaInfoTO, String paymentId, String cancellationId, OpTypeBO opType) {
        UserBO userBO = scaUtils.userBO(scaInfoTO.getUserId());
        PaymentBO payment = paymentService.getPaymentById(paymentId);
        int scaWeight = accessService.resolveScaWeightByDebtorAccount(userBO.getAccountAccesses(), payment.getDebtorAccount().getIban());

        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(opType, payment);
        String template = paymentKeyData.getTanTemplate();
        String authorisationId = opType == PAYMENT ? scaInfoTO.getAuthorisationId() : cancellationId;
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        scaResponseResolver.generateCodeAndUpdateResponse(paymentId, response, authorisationId, template, scaWeight, userBO, opType, scaInfoTO.getScaMethodId());

        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment.getDebtorAccount().getIban());
        scaResponseResolver.updateScaResponseFields(userBO, response, authorisationId, template, bearerToken, SCAMETHODSELECTED, scaWeight);
        return scaResponseResolver.updatePaymentRelatedResponseFields(response, payment);
    }

    private SCAPaymentResponseTO loadSca(ScaInfoTO scaInfoTO, String paymentId, String operationId, OpTypeBO opType) {
        SCAOperationBO scaOperation = scaOperationService.loadAuthCode(operationId);
        UserBO user = scaUtils.userBO(scaInfoTO.getUserId());
        PaymentBO payment = paymentService.getPaymentById(paymentId);
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(opType, payment);
        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment.getDebtorAccount().getIban());
        int scaWeight = accessService.resolveScaWeightByDebtorAccount(user.getAccountAccesses(), payment.getDebtorAccount().getIban());

        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        scaResponseResolver.updateScaResponseFields(user, response, operationId, paymentKeyData.getTanTemplate(), bearerToken, ScaStatusTO.valueOf(scaOperation.getScaStatus().name()), scaWeight);
        scaResponseResolver.updateScaUserDataInResponse(user, scaOperation, response);
        return scaResponseResolver.updatePaymentRelatedResponseFields(response, payment);
    }

    private BearerTokenTO paymentAccountAccessToken(ScaInfoTO scaInfoTO, String iban) {
        // Returned token can be used to access status.
        AisConsentBO aisConsent = new AisConsentBO();
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        aisConsent.setAccess(access);
        List<String> asList = Collections.singletonList(iban);
        access.setAccounts(asList);
        access.setTransactions(asList);
        access.setBalances(asList);
        aisConsent.setFrequencyPerDay(0);
        aisConsent.setRecurringIndicator(true);
        // This is the user login for psd2 and not the technical id.
        aisConsent.setUserId(scaInfoTO.getUserLogin());
        return bearerTokenMapper.toBearerTokenTO(authorizationService.consentToken(scaInfoMapper.toScaInfoBO(scaInfoTO), aisConsent));
    }

    private SCAPaymentResponseTO authorizeOperation(ScaInfoTO scaInfoTO, String paymentId, String cancellationId, OpTypeBO opType) {
        PaymentBO payment = paymentService.getPaymentById(paymentId);
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(opType, payment);

        String authorisationId = opType == PAYMENT ? scaInfoTO.getAuthorisationId() : cancellationId;
        ScaValidationBO scaValidationBO = validateAuthCode(scaInfoTO.getUserId(), payment, authorisationId, scaInfoTO.getAuthCode(), paymentKeyData.getTanTemplate());
        if (scaOperationService.authenticationCompleted(paymentId, opType)) {
            if (opType == PAYMENT) {
                paymentService.updatePaymentStatus(paymentId, ACTC);
                payment.setTransactionStatus(paymentService.executePayment(paymentId, scaInfoTO.getUserLogin()));
            } else {
                payment.setTransactionStatus(paymentService.cancelPayment(paymentId));
            }
        } else if (multilevelScaEnable) {
            payment.setTransactionStatus(paymentService.updatePaymentStatus(paymentId, PATC));
        }
        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment.getDebtorAccount().getIban());
        UserBO userBO = scaUtils.userBO(scaInfoTO.getUserId());
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        response.setAuthConfirmationCode(scaValidationBO.getAuthConfirmationCode());
        int scaWeight = accessService.resolveScaWeightByDebtorAccount(userBO.getAccountAccesses(), payment.getDebtorAccount().getIban());
        scaResponseResolver.updateScaResponseFields(userBO, response, authorisationId, paymentKeyData.getTanTemplate(), bearerToken, ScaStatusTO.valueOf(scaValidationBO.getScaStatus().toString()), scaWeight);
        return scaResponseResolver.updatePaymentRelatedResponseFields(response, payment);
    }

    private ScaValidationBO validateAuthCode(String userId, PaymentBO payment, String authorisationId, String authCode, String template) {
        UserBO userBO = scaUtils.userBO(userId);
        int scaWeight = accessService.resolveScaWeightByDebtorAccount(userBO.getAccountAccesses(), payment.getDebtorAccount().getIban());
        return scaOperationService.validateAuthCode(authorisationId, payment.getPaymentId(), template, authCode, scaWeight);
    }

}

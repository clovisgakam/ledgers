package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.payment.ConsentKeyDataTO;
import de.adorsys.ledgers.middleware.api.domain.sca.AuthorisationType;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaResponse;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareScaService;
import de.adorsys.ledgers.middleware.impl.converter.*;
import de.adorsys.ledgers.sca.domain.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.ScaInfoBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.AuthorizationService;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    @Value("${default.token.lifetime.seconds:600}")
    private int defaultLoginTokenExpireInSeconds;

    @Value("${sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

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
        ScaResponse response = toScaConsentResponse(userMapper.toUserTO(user), consent, consentKeyData.template(), scaOperationBO);
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
        ScaResponse response = toScaConsentResponse(userMapper.toUserTO(userBO), consent, consentKeyData.template(), scaOperationBO);
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
        ScaResponse response = toScaConsentResponse(userTO, consent, consentKeyData.template(), scaOperationBO);
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
    /*
     * The SCA requirement shall be added as property of a deposit account permission.
     *
     * For now we will assume there is no sca requirement, when the user having access
     * to the account does not habe any sca data configured.
     */

    private boolean scaRequired(UserBO user) {
        return scaUtils.hasSCA(user);
    }

    private ScaResponse prepareSCA(ScaInfoTO scaInfoTO, UserBO user, AisConsentTO aisConsent, ConsentKeyDataTO consentKeyData) {
        String consentKeyDataTemplate = consentKeyData.template();
        UserTO userTo = scaUtils.user(user);
        String authorisationId = scaUtils.authorisationId(scaInfoTO);
        if (!scaRequired(user)) {
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
            ScaResponse response = toScaConsentResponse(userTo, consentBO, consentKeyDataTemplate, scaOperationBO);
            response.setMultilevelScaRequired(multilevelScaEnable && scaWeight < 100);
            return response;
        }
    }

    private ScaResponse toScaConsentResponse(UserTO user, AisConsentBO consent, String messageTemplate, SCAOperationBO operation) {
        ScaResponse response = new ScaResponse();
        response.setAuthorisationType(AuthorisationType.AIS);
        scaResponseResolver.completeResponse(response, operation, user, messageTemplate, null);
        response.setParentId(consent.getId());
        return response;
    }
}

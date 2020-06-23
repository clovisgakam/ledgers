package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaResponse;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;

public interface MiddlewareScaService {


    // AIS

    /**
     * Authorizes a consent request. If the authentication is completed, the returned response will contain a valid bearer token.
     *
     * @param scaInfoTO : SCA information
     * @param consentId : the cosent id
     * @return SCAConsentResponseTO : the consent response.
     */
    ScaResponse authorizeConsent(ScaInfoTO scaInfoTO, String consentId);

    /**
     * Start an account consent process.
     *
     * @param scaInfoTO  SCA information
     * @param consentId  : the cosent id.
     * @param aisConsent : the consent details
     * @return the corresponding access token describing the account access
     */
    ScaResponse startScaAis(ScaInfoTO scaInfoTO, String consentId, AisConsentTO aisConsent);

    ScaResponse getScaAis(String userId, String consentId, String authorisationId);

    ScaResponse selectScaMethodAis(String userId, String consentId, String authorisationId, String scaMethodId);


    // PIS

    /**
     * PROC: 02c
     * <p>
     * This is called when the user enters the received code.
     *
     * @param scaInfoTO : SCA information
     * @param paymentId : the payment id
     * @return : auth response.
     */
    ScaResponse authorizePayment(ScaInfoTO scaInfoTO, String paymentId);

    ScaResponse authorizeCancelPayment(ScaInfoTO scaInfoTO, String paymentId, String cancellationId);

    ScaResponse getScaPis(ScaInfoTO scaInfoTO, String paymentId);

    ScaResponse getScaPisCancel(ScaInfoTO scaInfoTO, String paymentId, String cancellationId);

    ScaResponse selectScaMethodPis(ScaInfoTO scaInfoTO, String paymentId);

    ScaResponse selectScaMethodPisCancel(ScaInfoTO scaInfoTO, String paymentId, String cancellationId);

}

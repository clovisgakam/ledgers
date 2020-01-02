package de.adorsys.ledgers.middleware.api.service;

public interface EmailVerificationService {

    /**
     * Return boolean value if email with link for user confirmation was send
     *
     * @param userId id of user initiating operation
     */
    boolean sendVerificationToken(String userId);

    /**
     * Return boolean value if user was confirmed
     *
     * @param token verification token
     */
    boolean confirmVerificationToken(String token);
}

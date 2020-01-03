package de.adorsys.ledgers.middleware.api.service;

public interface EmailVerificationService {

    /**
     * Create a verification token for user
     *
     * @param userId User identifier
     * @return a token
     */
    String createVerificationToken(String userId);

    /**
     * Send email with link for user confirmation
     *
     * @param token verification token
     */
    void sendVerificationEmail(String token);

    /**
     * Confirm user
     *
     * @param token verification token
     */
    void confirmUser(String token);
}

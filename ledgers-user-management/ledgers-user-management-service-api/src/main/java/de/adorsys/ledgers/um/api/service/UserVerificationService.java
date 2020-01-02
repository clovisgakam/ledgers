package de.adorsys.ledgers.um.api.service;

public interface UserVerificationService {

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
     * @return boolean value if email was send
     */
    boolean sendVerificationEmail(String token);

    /**
     * Confirm user
     *
     * @param token verification token
     * @return boolean value if user was confirmed
     */
    boolean confirmUser(String token);
}

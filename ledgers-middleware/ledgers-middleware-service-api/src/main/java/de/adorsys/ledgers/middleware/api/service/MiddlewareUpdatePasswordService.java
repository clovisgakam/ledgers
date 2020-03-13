package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.security.UpdatePassword;

public interface MiddlewareUpdatePasswordService {

    void updatePassword(UpdatePassword resetPassword);

}

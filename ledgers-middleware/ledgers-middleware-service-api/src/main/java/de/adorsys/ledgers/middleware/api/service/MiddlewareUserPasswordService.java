package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.security.ResetPassword;
import de.adorsys.ledgers.security.SendCode;

public interface MiddlewareUserPasswordService {

    SendCode sendCode(ResetPassword resetPassword);

    void updatePassword(ResetPassword resetPassword);
}

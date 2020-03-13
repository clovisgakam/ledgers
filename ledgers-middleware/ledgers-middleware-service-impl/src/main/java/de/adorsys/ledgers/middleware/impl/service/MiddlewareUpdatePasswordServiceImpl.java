package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.service.MiddlewareUpdatePasswordService;
import de.adorsys.ledgers.security.UpdatePassword;
import de.adorsys.ledgers.um.api.service.UpdatePasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiddlewareUpdatePasswordServiceImpl implements MiddlewareUpdatePasswordService {
    private final UpdatePasswordService resetPasswordService;

    @Override
    public void updatePassword(UpdatePassword updatePassword) {
        resetPasswordService.updatePassword(updatePassword.getUserId(), updatePassword.getNewPassword());
    }
}

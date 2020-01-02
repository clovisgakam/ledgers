package de.adorsys.ledgers.middleware.impl.service.upload;

import de.adorsys.ledgers.middleware.api.service.EmailVerificationService;
import de.adorsys.ledgers.um.api.service.UserVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private final UserVerificationService userVerificationService;

    @Override
    public boolean sendVerificationToken(String userId) {
        String token = userVerificationService.createVerificationToken(userId);
        return userVerificationService.sendVerificationEmail(token);
    }

    @Override
    public boolean confirmVerificationToken(String token) {
        return userVerificationService.confirmUser(token);
    }
}
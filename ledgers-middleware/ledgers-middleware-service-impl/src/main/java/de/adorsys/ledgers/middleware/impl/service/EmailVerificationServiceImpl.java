package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.service.EmailVerificationService;
import de.adorsys.ledgers.um.api.domain.EmailVerificationBO;
import de.adorsys.ledgers.um.api.domain.EmailVerificationStatusBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserTypeBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.api.service.UserVerificationService;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.*;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final EmailVerificationStatusBO STATUS_VERIFIED = EmailVerificationStatusBO.VERIFIED;
    private static final EmailVerificationStatusBO STATUS_PENDING = EmailVerificationStatusBO.PENDING;

    //TODO use @ConfigurationProperties
    @Value("${verify.token.template.message}")
    private String message;

    @Value("${verify.token.template.subject}")
    private String subject;

    @Value("${verify.token.template.from}")
    private String from;

    @Value("${verify.email.base_path}")
    private String basePath;

    @Value("${verify.email.endpoint}")
    private String endpoint;

    private final UserVerificationService userVerificationService;
    private final UserService userService;

    @Override
    public String createVerificationToken(String userId) {
        UserBO userBO = userService.findById(userId);
        EmailVerificationBO emailVerification;
        try {
            emailVerification = userVerificationService.findByUserIdAndStatusNot(userId, STATUS_VERIFIED);
            emailVerification.updateToken();
        } catch (UserManagementModuleException e) {
            emailVerification = new EmailVerificationBO();
            emailVerification.createToken();
            emailVerification.setUser(userBO);
        }
        userVerificationService.updateEmailVerification(emailVerification);
        return emailVerification.getToken();
    }

    @Override
    public void sendVerificationEmail(String token) {
        EmailVerificationBO emailVerificationBO = userVerificationService.findByToken(token);
        UserBO user = emailVerificationBO.getUser();
        userVerificationService.sendMessage(subject, from, user.getEmail(), emailVerificationBO.formatMessage(message, basePath, endpoint, emailVerificationBO.getToken(), emailVerificationBO.getExpiredDateTime(), user.getEmail()));
    }

    @Override
    public void confirmUser(String token) {
        EmailVerificationBO emailVerification = userVerificationService.findByTokenAndStatus(token, STATUS_PENDING);
        if (emailVerification.isExpired()) {
            throw UserManagementModuleException.builder()
                          .errorCode(EXPIRED_TOKEN)
                          .devMsg(String.format("Verification token for user %s is expired for confirmation", emailVerification.getUser().getLogin()))
                          .build();
        }

        UserBO userBO = userService.findById(emailVerification.getUser().getId());
        emailVerification.setConfirmedDateTime(LocalDateTime.now());
        emailVerification.setStatus(EmailVerificationStatusBO.VERIFIED);
        userBO.setUserType(UserTypeBO.REAL);
        userVerificationService.updateEmailVerification(emailVerification);
        userService.updateUser(userBO);
    }
}
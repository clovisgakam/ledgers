package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.service.EmailVerificationService;
import de.adorsys.ledgers.um.api.domain.EmailVerificationBO;
import de.adorsys.ledgers.um.api.domain.EmailVerificationStatusBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.service.ScaUserDataService;
import de.adorsys.ledgers.um.api.service.ScaVerificationService;
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

    private final ScaVerificationService scaVerificationService;
    private final ScaUserDataService scaUserDataService;

    @Override
    public String createVerificationToken(String email) {
        ScaUserDataBO scaUserDataBO = scaUserDataService.findByEmail(email);
        EmailVerificationBO emailVerification;
        try {
            emailVerification = scaVerificationService.findByScaIdAndStatusNot(scaUserDataBO.getId(), STATUS_VERIFIED);
            emailVerification.updateToken();
        } catch (UserManagementModuleException e) {
            emailVerification = new EmailVerificationBO();
            emailVerification.createToken();
            emailVerification.setScaUserData(scaUserDataBO);
        }
        scaVerificationService.updateEmailVerification(emailVerification);
        return emailVerification.getToken();
    }

    @Override
    public void sendVerificationEmail(String token) {
        EmailVerificationBO emailVerificationBO = scaVerificationService.findByToken(token);
        ScaUserDataBO scaUserDataBO = emailVerificationBO.getScaUserData();
        scaVerificationService.sendMessage(subject, from, scaUserDataBO.getMethodValue(), emailVerificationBO.formatMessage(message, basePath, endpoint, emailVerificationBO.getToken(), emailVerificationBO.getExpiredDateTime(), scaUserDataBO.getMethodValue()));
    }

    @Override
    public void confirmUser(String token) {
        EmailVerificationBO emailVerification = scaVerificationService.findByTokenAndStatus(token, STATUS_PENDING);
        if (emailVerification.isExpired()) {
            throw UserManagementModuleException.builder()
                          .errorCode(EXPIRED_TOKEN)
                          .devMsg(String.format("Verification token for email %s is expired for confirmation", emailVerification.getScaUserData().getMethodValue()))
                          .build();
        }

        ScaUserDataBO scaUserDataBO = scaUserDataService.findByEmail(emailVerification.getScaUserData().getMethodValue());
        emailVerification.setConfirmedDateTime(LocalDateTime.now());
        emailVerification.setStatus(EmailVerificationStatusBO.VERIFIED);
        scaUserDataBO.setValid(true);
        scaVerificationService.updateEmailVerification(emailVerification);
        scaUserDataService.updateScaUserData(scaUserDataBO);
    }
}
package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserTypeBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.api.service.UserVerificationService;
import de.adorsys.ledgers.um.db.domain.EmailVerificationStatus;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.domain.EmailVerificationEntity;
import de.adorsys.ledgers.um.db.repository.EmailVerificationRepository;
import de.adorsys.ledgers.um.impl.converter.UserConverter;
import de.adorsys.ledgers.um.impl.service.password.UserMailSender;

import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserVerificationServiceImpl implements UserVerificationService {

    private static final EmailVerificationStatus VERIFIED = EmailVerificationStatus.VERIFIED;
    private static final EmailVerificationStatus PENDING = EmailVerificationStatus.PENDING;

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

    private final UserService userService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserMailSender userMailSender;
    private final UserConverter userConverter;

    @Override
    public String createVerificationToken(String userId) {
        UserBO userBO = userService.findById(userId);
        Optional<EmailVerificationEntity> verificationToken = emailVerificationRepository.findByUserIdAndStatusNot(userId, VERIFIED);
        EmailVerificationEntity emailEntity;
        if (verificationToken.isPresent()) {
            emailEntity = verificationToken.get();
            emailEntity.doUpdateToken();
        } else {
            emailEntity = new EmailVerificationEntity();
            emailEntity.setUser(userConverter.toUserPO(userBO));
        }
        emailVerificationRepository.save(emailEntity);
        return emailEntity.getToken();
    }

    @Override
    public boolean sendVerificationEmail(String token) {
        EmailVerificationEntity verificationToken = emailVerificationRepository.findByToken(token)
                                                            .orElseThrow(() -> UserManagementModuleException.builder()
                                                                                       .errorCode(TOKEN_NOT_FOUND)
                                                                                       .devMsg(String.format("Verification token not found: %s", token))
                                                                                       .build());

        UserEntity user = verificationToken.getUser();
        return userMailSender.send(subject, from, user.getEmail(), formatMessage(message, verificationToken, verificationToken.getExpiredDateTime(), user.getEmail()));
    }

    private String formatMessage(String message, EmailVerificationEntity token, LocalDateTime date, String email) {
        return String.format(message, basePath + endpoint + "?verificationToken=" + token.getToken(), date.getMonth().toString() + " " + date.getDayOfMonth() + ", " + date.getYear() + " " + date.getHour() + ":" + date.getMinute(), email);
    }

    @Override
    public boolean confirmUser(String token) {
        EmailVerificationEntity verificationToken = emailVerificationRepository.findByTokenAndStatus(token, PENDING)
                                                            .orElseThrow(() -> UserManagementModuleException.builder()
                                                                                       .errorCode(INVALID_VERIFICATION_TOKEN)
                                                                                       .devMsg(String.format("Invalid verification token %s or user is already confirmed", token))
                                                                                       .build());

        if (verificationToken.getExpiredDateTime().isBefore(LocalDateTime.now())) {
            throw UserManagementModuleException.builder()
                          .errorCode(EXPIRED_TOKEN)
                          .devMsg(String.format("Verification token for user %s is expired for confirmation", verificationToken.getUser().getLogin()))
                          .build();
        }

        UserBO userBO = userService.findById(verificationToken.getUser().getId());
        verificationToken.setConfirmedDateTime(LocalDateTime.now());
        verificationToken.setStatus(EmailVerificationStatus.VERIFIED);
        userBO.setUserType(UserTypeBO.REAL);
        emailVerificationRepository.save(verificationToken);
        userService.updateUser(userBO);
        return true;
    }
}
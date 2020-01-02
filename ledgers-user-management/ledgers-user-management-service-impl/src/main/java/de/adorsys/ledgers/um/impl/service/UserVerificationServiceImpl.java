package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.service.UserVerificationService;
import de.adorsys.ledgers.um.db.domain.EmailVerificationStatus;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.domain.UserType;
import de.adorsys.ledgers.um.db.domain.EmailVerificationEntity;
import de.adorsys.ledgers.um.db.repository.UserRepository;
import de.adorsys.ledgers.um.db.repository.EmailVerificationRepository;
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

    private static final String USER_WITH_ID_NOT_FOUND = "User with id %s not found";
    private static final String VERIFICATION_TOKEN_NOT_FOUND = "Verification token not found: %s";
    private static final String INVALID_TOKEN = "Invalid verification token %s for user confirmation";
    private static final String USER_CONFIRMED = "User with login %s is already confirmed";
    private static final String EXPIRED = "Verification token for user %s is expired for confirmation";

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserMailSender userMailSender;

    @Override
    public String createVerificationToken(String userId) {
        Optional<UserEntity> userEntity = userRepository.findById(userId);
        if (!userEntity.isPresent()) {
            throw UserManagementModuleException.builder()
                          .errorCode(TOKEN_CREATION_ERROR)
                          .devMsg(String.format(USER_WITH_ID_NOT_FOUND, userId))
                          .build();
        }
        Optional<EmailVerificationEntity> verificationToken = emailVerificationRepository.findByUserIdAndStatusNot(userId, EmailVerificationStatus.VERIFIED);
        EmailVerificationEntity token;
        if (verificationToken.isPresent()) {
            token = verificationToken.get();
            token.doUpdateToken();
        } else {
            token = new EmailVerificationEntity();
            token.setUser(userEntity.get());
        }
        emailVerificationRepository.save(token);
        return token.getToken();
    }

    @Override
    public boolean sendVerificationEmail(String token) {
        Optional<EmailVerificationEntity> verificationToken = emailVerificationRepository.findByToken(token);
        if (!verificationToken.isPresent()) {
            throw UserManagementModuleException.builder()
                          .errorCode(TOKEN_NOT_FOUND)
                          .devMsg(String.format(VERIFICATION_TOKEN_NOT_FOUND, token))
                          .build();
        }
        EmailVerificationEntity emailVerificationEntity = verificationToken.get();
        UserEntity user = emailVerificationEntity.getUser();
        return userMailSender.send(subject, from, user.getEmail(), formatMessage(message, emailVerificationEntity, emailVerificationEntity.getExpiredDateTime(), user.getEmail()));
    }

    private String formatMessage(String message, EmailVerificationEntity token, LocalDateTime date, String email) {
        return String.format(message, basePath + endpoint + "?verificationToken=" + token.getToken(), date.getMonth().toString() + " " + date.getDayOfMonth() + ", " + date.getYear() + " " + date.getHour() + ":" + date.getMinute(), email);
    }

    @Override
    public boolean confirmUser(String token) {
        Optional<EmailVerificationEntity> verificationToken = emailVerificationRepository.findByToken(token);
        if (!verificationToken.isPresent()) {
            throw UserManagementModuleException.builder()
                          .errorCode(INVALID_VERIFICATION_TOKEN)
                          .devMsg(String.format(INVALID_TOKEN, token))
                          .build();
        }

        EmailVerificationEntity emailVerificationEntity = verificationToken.get();
        if (emailVerificationEntity.getExpiredDateTime().isBefore(LocalDateTime.now())) {
            throw UserManagementModuleException.builder()
                          .errorCode(EXPIRED_TOKEN)
                          .devMsg(String.format(EXPIRED, emailVerificationEntity.getUser().getLogin()))
                          .build();
        }

        Optional<UserEntity> userEntity = userRepository.findById(emailVerificationEntity.getUser().getId());
        UserEntity user = userEntity.get();

        if (emailVerificationEntity.getStatus() == EmailVerificationStatus.VERIFIED) {
            throw UserManagementModuleException.builder()
                          .errorCode(USER_ALREADY_CONFIRM)
                          .devMsg(String.format(USER_CONFIRMED, user.getLogin()))
                          .build();
        }

        emailVerificationEntity.setConfirmedDateTime(LocalDateTime.now());
        emailVerificationEntity.setStatus(EmailVerificationStatus.VERIFIED);
        user.setUserType(UserType.REAL);
        emailVerificationRepository.save(emailVerificationEntity);
        userRepository.save(user);
        return true;
    }
}
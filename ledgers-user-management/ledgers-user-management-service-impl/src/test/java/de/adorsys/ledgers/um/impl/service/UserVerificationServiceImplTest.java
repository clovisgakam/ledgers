package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.db.domain.EmailVerificationEntity;
import de.adorsys.ledgers.um.db.domain.EmailVerificationStatus;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.repository.EmailVerificationRepository;
import de.adorsys.ledgers.um.db.repository.UserRepository;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pro.javatar.commons.reader.ResourceReader;
import pro.javatar.commons.reader.YamlReader;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserVerificationServiceImplTest {

    @InjectMocks
    private UserVerificationServiceImpl userVerificationService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    private ResourceReader reader = YamlReader.getInstance();

    private static final String USER_ID = "SomeUniqueID";
    private static final String VERIFICATION_TOKEN = "Hccf3sWaSrgpBVjBx0osHk";
    private static final EmailVerificationStatus VERIFIED = EmailVerificationStatus.VERIFIED;
    private static final EmailVerificationStatus PENDING = EmailVerificationStatus.PENDING;
    private static final LocalDateTime DATE = LocalDateTime.now();
    private UserEntity userEntity;

    @Before
    public void setUp() {
        userEntity = readUserEntity();
    }

    @Test
    public void createVerificationToken() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.ofNullable(userEntity));
        when(emailVerificationRepository.findByUserIdAndStatusNot(USER_ID, VERIFIED)).thenReturn(Optional.ofNullable(getVerificationToken(DATE.plusWeeks(1))));

        String token = userVerificationService.createVerificationToken(USER_ID);
        assertTrue(!token.isEmpty());
    }

    @Test
    public void confirmUser() {
        when(emailVerificationRepository.findByTokenAndStatus(VERIFICATION_TOKEN, PENDING)).thenReturn(Optional.ofNullable(getVerificationToken(DATE.plusWeeks(1))));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.ofNullable(userEntity));

        boolean isConfirmed = userVerificationService.confirmUser(VERIFICATION_TOKEN);
        assertTrue(isConfirmed);
    }

    @Test(expected = UserManagementModuleException.class)
    public void confirmUser_expiredToken() {
        when(emailVerificationRepository.findByTokenAndStatus(VERIFICATION_TOKEN, PENDING)).thenReturn(Optional.ofNullable(getVerificationToken(DATE.minusWeeks(2))));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.ofNullable(userEntity));

        userVerificationService.confirmUser(VERIFICATION_TOKEN);
    }

    private UserEntity readUserEntity() {
        try {
            return reader.getObjectFromResource(getClass(), "user-entity.yml", UserEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private EmailVerificationEntity getVerificationToken(LocalDateTime date) {
        EmailVerificationEntity entity = new EmailVerificationEntity();
        entity.setToken(VERIFICATION_TOKEN);
        entity.setIssuedDateTime(DATE);
        entity.setExpiredDateTime(date);
        entity.setUser(userEntity);
        return entity;
    }
}
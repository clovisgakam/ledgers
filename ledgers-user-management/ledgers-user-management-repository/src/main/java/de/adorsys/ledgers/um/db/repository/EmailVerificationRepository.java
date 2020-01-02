package de.adorsys.ledgers.um.db.repository;

import de.adorsys.ledgers.um.db.domain.EmailVerificationEntity;
import de.adorsys.ledgers.um.db.domain.EmailVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, String> {

    Optional<EmailVerificationEntity> findByUserIdAndStatusNot(String userId, EmailVerificationStatus status);

    Optional<EmailVerificationEntity> findByTokenAndStatus(String token, EmailVerificationStatus status);

    Optional<EmailVerificationEntity> findByToken(String token);
}

package de.adorsys.ledgers.idp.repository.repository;

import de.adorsys.ledgers.idp.repository.domain.UserEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface IdpUserRepository extends PagingAndSortingRepository<UserEntity, Long> {
    @NotNull
    List<UserEntity> findAll();

    Optional<UserEntity> findByLogin(String login);
}

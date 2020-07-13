package de.adorsys.ledgers.idp.service.api.service;

import de.adorsys.ledgers.idp.core.domain.UserStatus;
import de.adorsys.ledgers.idp.service.api.domain.IdpUser;

import java.util.List;

public interface UserService {

    IdpUser getById(long userId);

    IdpUser findByLogin(String login);

    List<IdpUser> getAllUsers();

    IdpUser registerUser(IdpUser user);

    IdpUser updateUser(IdpUser user);

    void updateStatus(long userId, UserStatus status);
}

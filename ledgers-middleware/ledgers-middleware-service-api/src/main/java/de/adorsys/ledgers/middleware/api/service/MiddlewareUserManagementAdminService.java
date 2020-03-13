package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.um.UserCredentialsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserLoginTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;

public interface MiddlewareUserManagementAdminService {

    UserTO create(String branchId, UserTO createUser);

    UserLoginTO login(UserCredentialsTO credentials);
}

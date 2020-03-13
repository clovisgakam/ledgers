package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.um.UserCredentialsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserLoginTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUpdatePasswordService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementAdminService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.security.UpdatePassword;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.CUSTOMER;
import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.STAFF;
import static java.util.Arrays.asList;

@Slf4j
@RestController
@MiddlewareUserResource
@RequiredArgsConstructor
@RequestMapping(UserManagementAdminRestAPI.BASE_PATH)
public class UserManagementAdminResource implements UserManagementAdminRestAPI {
    private final MiddlewareUserManagementService middlewareUserService;
    private final MiddlewareUpdatePasswordService middlewareUpdatePasswordService;
    private final MiddlewareUserManagementAdminService middlewareUserManagementAdminService;

    // TODO use another TO model without ROLE
    @Override
    public ResponseEntity<UserLoginTO> login(UserCredentialsTO userCredentials) {
        return ResponseEntity.ok(middlewareUserManagementAdminService.login(userCredentials));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePassword(UpdatePassword updatePassword) {
        middlewareUpdatePasswordService.updatePassword(updatePassword);
        return ResponseEntity.accepted().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomPageImpl<UserTO>> getUsersByBranch(String userId, String queryParam, int page, int size) {
        UserTO branchStaff = middlewareUserService.findById(userId);
        return ResponseEntity.ok(middlewareUserService.getUsersByBranchAndRoles(branchStaff.getBranch(),
                                                                                asList(CUSTOMER, STAFF),
                                                                                queryParam,
                                                                                new CustomPageableImpl(page, size)));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomPageImpl<UserTO>> getUsers(UserRoleTO role, String queryParam, int page, int size) {
        return ResponseEntity.ok(middlewareUserService.getUsersByRole(role, queryParam, new CustomPageableImpl(page, size)));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserTO> createUser(String userId, UserTO user) {
        return ResponseEntity.ok(middlewareUserManagementAdminService.create(userId, user));
    }

}

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserCredentialsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserLoginTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementAdminService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.PasswordEnc;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.CUSTOMER;
import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.STAFF;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.INSUFFICIENT_PERMISSION;

@Service
@RequiredArgsConstructor
public class MiddlewareUserManagementAdminServiceImpl implements MiddlewareUserManagementAdminService {
    @Value("${application.security.admin_default_password:admin123}")
    private String adminDefaultPassword;

    private final MiddlewareUserManagementService middlewareUserService;
    private final MiddlewareOnlineBankingService onlineBankingService;
    private final UserService userService;
    private final PasswordEnc passwordEnc;


    @Override
    public UserLoginTO login(UserCredentialsTO credentials) {
        SCALoginResponseTO response = onlineBankingService.authorise(credentials.getLogin(), credentials.getPin(), UserRoleTO.ADMIN);
        UserBO userBO = userService.findByLogin(credentials.getLogin());
        return new UserLoginTO(response.getBearerToken(), passwordEnc.verify(userBO.getId(), adminDefaultPassword, userBO.getPin()));
    }

    @Override
    public UserTO create(String branchId, UserTO user) {
        // TODO move to factory
        if (user.getUserRoles().contains(CUSTOMER)) {
            updateCustomerUser(branchId, user);
        } else if (user.getUserRoles().contains(STAFF)) {
            updateStaffUser(branchId, user);
        }
        UserTO newUser = middlewareUserService.create(user);
        newUser.setPin(null);
        return newUser;
    }

    private void updateStaffUser(String branchId, UserTO user) {
        if (middlewareUserService.countUsersByBranch(branchId) > 0) {
            throw MiddlewareModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .build();
        }
        user.setBranch(branchId);
        user.setUserRoles(Collections.singletonList(STAFF));
    }

    private void updateCustomerUser(String branchId, UserTO user) {
        UserTO branchStaff = middlewareUserService.findById(branchId);
        branchStaff.setUserRoles(Collections.singletonList(CUSTOMER));
        user.setBranch(branchStaff.getBranch());
    }
}

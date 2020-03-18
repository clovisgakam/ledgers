package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.um.UserCredentialsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserLoginTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.security.UpdatePassword;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "LDG014 - User management (ADMIN access)")
public interface UserManagementAdminRestAPI {
    String BASE_PATH = "/admin-access" + UserMgmtRestAPI.BASE_PATH;

    /**
     * Authorize returns a bearer token that can be reused by the consuming application.
     *
     * @param userCredentials tpp login and tpp pin
     * @return JWT token and user info
     */
    @ApiOperation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Login")
    @PostMapping("/login")
    ResponseEntity<UserLoginTO> login(@RequestBody UserCredentialsTO userCredentials);

    @ApiOperation(value = "Update user password", authorizations = @Authorization(value = "apiKey"))
    @PutMapping("/password")
    ResponseEntity<Void> updatePassword(@RequestBody UpdatePassword updatePassword);

    /**
     * Lists users within the branch
     *
     * @return list of users for the branch
     */
    @ApiOperation(value = "Lists users by branch", notes = "Lists users by branch.", authorizations = @Authorization(value = "apiKey"))
    @GetMapping("/{userId}")
    ResponseEntity<CustomPageImpl<UserTO>> getUsersByBranch(
            @PathVariable("userId") String userId,
            @RequestParam(value = "queryParam", defaultValue = "", required = false) String queryParam,
            @RequestParam("page") int page,
            @RequestParam("size") int size);

    /**
     * Lists all users
     *
     * @return list of users
     */
    @ApiOperation(value = "Lists users by role", notes = "Lists users by roles.", authorizations = @Authorization(value = "apiKey"))
    @GetMapping
    ResponseEntity<CustomPageImpl<UserTO>> getUsers(@RequestParam("role") UserRoleTO role,
                                                    @RequestParam(value = "queryParam", defaultValue = "", required = false) String queryParam,
                                                    @RequestParam("page") int page,
                                                    @RequestParam("size") int size);

    /**
     * Creates new user within the same branch
     *
     * @param user user to be created
     * @return created user
     */
    @ApiOperation(value = "Create user", notes = "Create new user with the same branch as creator.", authorizations = @Authorization(value = "apiKey"))
    @PostMapping("/{userId}")
    ResponseEntity<UserTO> createUser(@PathVariable("userId") String userId, @RequestBody UserTO user);
}

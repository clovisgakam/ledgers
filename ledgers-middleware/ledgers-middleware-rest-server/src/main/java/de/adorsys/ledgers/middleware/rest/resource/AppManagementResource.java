/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(AppManagementResource.BASE_PATH)
@Api(tags = "Management" , description= "Application management")
@MiddlewareUserResource
public class AppManagementResource {

	public static final String BASE_PATH = "/management/app";
	public static final String ADMIN_PATH = "/admin";
	public static final String INIT_PATH = "/init";
	
	private static final Logger logger = LoggerFactory.getLogger(AppManagementResource.class);
	
	@Autowired
    private AppManagementService appManagementService;
	
	@Autowired
	private MiddlewareUserManagementService userManagementService; 
	@Autowired
    private MiddlewareOnlineBankingService middlewareUserService;

    @GetMapping("/ping")
    @ApiOperation("Echo the server")
    public ResponseEntity<String> ping() {
    	return ResponseEntity.ok("pong");
    }
	
    @PostMapping(INIT_PATH)
    @ApiOperation("Initializes the deposit account module.")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Void> initApp() {
    	try {
			appManagementService.initApp();
			return ResponseEntity.ok().build();
		} catch (IOException e) {
			throw new IllegalStateException("Error initializing deposit account module.", e);
		}
    }
    
    @PostMapping(ADMIN_PATH)
    @ApiOperation(value="Creates the admin account. This is only done if the application has no account yet. Returns a bearer token admin can use to proceed with further operations.")
    @SuppressWarnings("PMD.IdenticalCatchBranches")
    public ResponseEntity<String> admin(@RequestBody(required=true) UserTO adminUser){
    	List<UserTO> users = userManagementService.listUsers(0, 1);
    	if(!users.isEmpty()) {
    		final String ADMIN_FIRST = "Admin user can not be created after initialization. This must be the first user of the system.";
            logger.error(ADMIN_FIRST);
            throw new ForbiddenRestException("Can not create admin user.").withDevMessage(ADMIN_FIRST);    		
    	}
    	UserTO user = new UserTO();
    	user.setLogin(adminUser.getLogin());
    	user.setPin(adminUser.getPin());
    	user.setEmail(adminUser.getEmail());
    	user.getUserRoles().add(UserRoleTO.SYSTEM);
		try {
			userManagementService.create(user);
		} catch (UserAlreadyExistsMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ConflictRestException(e.getMessage()).withDevMessage(e.getMessage());
		}
		
		try {
			String accessToken = middlewareUserService.authorise(adminUser.getLogin(), adminUser.getPin(), UserRoleTO.SYSTEM);
			return ResponseEntity.ok(accessToken);
		} catch (UserNotFoundMiddlewareException e) {
			String USER_NOT_FOUND_SHALL_NOT_HAPPEN = "Shall not happen. We just created admin user.";
            logger.error(USER_NOT_FOUND_SHALL_NOT_HAPPEN, e);
			throw new IllegalStateException(USER_NOT_FOUND_SHALL_NOT_HAPPEN, e);
		} catch (InsufficientPermissionMiddlewareException e) {
			String ISSUFICIENT_PERM_SHALL_NOT_HAPPEN = "Unknownd exception, shall not happen.";
            logger.error(ISSUFICIENT_PERM_SHALL_NOT_HAPPEN, e);
			throw new IllegalStateException(ISSUFICIENT_PERM_SHALL_NOT_HAPPEN, e);
		} 
    }
}

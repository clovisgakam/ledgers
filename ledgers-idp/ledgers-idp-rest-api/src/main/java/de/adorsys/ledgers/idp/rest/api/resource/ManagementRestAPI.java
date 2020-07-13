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

package de.adorsys.ledgers.idp.rest.api.resource;

import de.adorsys.ledgers.idp.core.domain.UserStatus;
import de.adorsys.ledgers.idp.service.api.domain.IdpUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Management Rest Api")
public interface ManagementRestAPI {
    String BASE_PATH = "/management";

    @GetMapping("/users")
    @ApiOperation(value = "Get registered users", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<List<IdpUser>> users();

    @GetMapping("/user")
    @ApiOperation(value = "Get registered user by Id", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<IdpUser> userById(@RequestParam("userId") long userId);

    @PostMapping("/user")
    @ApiOperation(value = "Register new user", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<IdpUser> register(@RequestBody IdpUser user);

    @PutMapping("/status")
    @ApiOperation(value = "Change user status", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<Void> changeStatus(@RequestParam("userId") long userId, @RequestParam("status") UserStatus status);

    @PutMapping("/user")
    @ApiOperation(value = "Update user data", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<Void> updateUserData(IdpUser user);
}

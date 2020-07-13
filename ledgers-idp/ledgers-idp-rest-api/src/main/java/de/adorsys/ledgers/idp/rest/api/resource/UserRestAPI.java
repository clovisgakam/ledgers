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

import de.adorsys.ledgers.idp.core.domain.IdpToken;
import de.adorsys.ledgers.idp.service.api.domain.IdpUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "User Rest Api")
public interface UserRestAPI {
    String BASE_PATH = "/user";

    @GetMapping("/self")
    @ApiOperation(value = "Get Self users", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<IdpUser> users();

    @GetMapping("/token")
    @ApiOperation(value = "Introspect token", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<IdpToken> introspectToken();

    @PostMapping("/login")
    @ApiOperation(value = "Perform login")
    ResponseEntity<IdpToken> login(@RequestParam("login") String login, @RequestParam("password") String password);
}

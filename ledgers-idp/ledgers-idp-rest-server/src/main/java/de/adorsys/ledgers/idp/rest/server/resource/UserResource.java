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

package de.adorsys.ledgers.idp.rest.server.resource;

import de.adorsys.ledgers.idp.core.domain.IdpToken;
import de.adorsys.ledgers.idp.rest.api.resource.UserRestAPI;
import de.adorsys.ledgers.idp.rest.server.annotation.IdpResource;
import de.adorsys.ledgers.idp.service.api.domain.IdpUser;
import de.adorsys.ledgers.idp.service.api.service.IdpTokenService;
import de.adorsys.ledgers.idp.service.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@IdpResource
@RestController
@RequiredArgsConstructor
@RequestMapping(UserRestAPI.BASE_PATH)
public class UserResource implements UserRestAPI {
    private final UserService userService;
    private final IdpTokenService tokenService;

    @Override
    public ResponseEntity<IdpUser> users() {
        IdpToken token = (IdpToken) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        long userId = Long.parseLong(token.getAccessTokenObject().getSub());
        return ResponseEntity.ok(userService.getById(userId));
    }

    @Override
    public ResponseEntity<IdpToken> introspectToken() {
        IdpToken token = (IdpToken) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        return ResponseEntity.ok(tokenService.validate(token.getAccess_token()));
    }

    @Override
    public ResponseEntity<IdpToken> login(String login, String password) {
        return ResponseEntity.ok(tokenService.login(login, password));
    }
}

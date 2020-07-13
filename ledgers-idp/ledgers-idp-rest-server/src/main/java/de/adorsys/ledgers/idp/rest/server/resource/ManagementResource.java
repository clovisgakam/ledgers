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

import de.adorsys.ledgers.idp.core.domain.UserStatus;
import de.adorsys.ledgers.idp.rest.api.resource.ManagementRestAPI;
import de.adorsys.ledgers.idp.rest.server.annotation.IdpResource;
import de.adorsys.ledgers.idp.service.api.domain.IdpUser;
import de.adorsys.ledgers.idp.service.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@IdpResource
@RestController
@RequiredArgsConstructor
@RequestMapping(ManagementRestAPI.BASE_PATH)
public class ManagementResource implements ManagementRestAPI {
    private final UserService userService;

    @Override
    public ResponseEntity<List<IdpUser>> users() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Override
    public ResponseEntity<IdpUser> userById(long userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }

    @Override
    public ResponseEntity<IdpUser> register(IdpUser user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @Override
    public ResponseEntity<Void> changeStatus(long userId, UserStatus status) {
        userService.updateStatus(userId, status);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    public ResponseEntity<Void> updateUserData(IdpUser user) {
        userService.updateUser(user);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}

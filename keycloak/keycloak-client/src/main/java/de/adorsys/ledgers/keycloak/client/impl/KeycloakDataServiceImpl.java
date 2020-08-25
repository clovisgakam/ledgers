package de.adorsys.ledgers.keycloak.client.impl;

import de.adorsys.ledgers.keycloak.client.api.KeycloakDataService;
import de.adorsys.ledgers.keycloak.client.mapper.KeycloakDataMapper;
import de.adorsys.ledgers.keycloak.client.model.KeycloakClient;
import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakDataServiceImpl implements KeycloakDataService {
    private final Keycloak keycloak;
    private final KeycloakDataMapper mapper;

    @Override
    public void createRealm(String realmName) {
        keycloak.realms().create(mapper.createRealmRepresentation(realmName));
    }

    @Override
    public void createClient(KeycloakClient client) {
        keycloak.realm(client.getRealm()).clients().create(mapper.createClientRepresentation(client.getClientId(),
                                                                                      client.getClientSecret()));
    }

    @Override
    public boolean clientExists(String realm, String clientId) {
        List<ClientRepresentation> byClientId = keycloak.realm(realm).clients().findByClientId(clientId);
        return CollectionUtils.isNotEmpty(byClientId);
    }

    @Override
    public Optional<KeycloakUser> getUser(String realm, String login) {
        List<UserRepresentation> search = keycloak.realm(realm).users().search(login);
        if (CollectionUtils.isNotEmpty(search)) {
            return Optional.of(mapper.toKeycloakUser(search.get(0)));
        }
        return Optional.empty();
    }

    @Override
    public void createUser(String realm, KeycloakUser user) {
        Response response = keycloak.realm(realm).users().create(mapper.createUserRepresentation(user));
        if (HttpStatus.CREATED.value() == response.getStatus()) {
            String userId = CreatedResponseUtil.getCreatedId(response);
            log.info("User[login: {}] is created with id: ", userId);
        }
    }

    @Override
    public void updateUser(String realm, KeycloakUser user) {
        List<UserRepresentation> search = keycloak.realm(realm).users().search(user.getLogin());
        if (CollectionUtils.isNotEmpty(search)) {
            String userId = search.get(0).getId();
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            userResource.update(mapper.toUpdateUserPresentation(userResource.toRepresentation(), user));
            log.debug("User[login: {}] was updated in keycloak.", user.getLogin());
            return;
        }
        log.info("User[login: {}] was not found in keycloak.", user.getLogin());
    }

    @Override
    public void deleteUser(String realm, String login) {
        List<UserRepresentation> search = keycloak.realm(realm).users().search(login);
        if (CollectionUtils.isNotEmpty(search)) {
            String userId = search.get(0).getId();
            keycloak.realm(realm).users().delete(userId);
            log.info("User[login: {}] was deleted from keycloak.", login);
            return;
        }
        log.info("User[login: {}] was not found in keycloak.", login);
    }

    @Override
    public boolean userExists(String realm, String login) {
        List<UserRepresentation> search = keycloak.realm(realm).users().search(login);
        return CollectionUtils.isNotEmpty(search);
    }
}

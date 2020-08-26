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
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakDataServiceImpl implements KeycloakDataService {
    private final Keycloak keycloak;
    private final KeycloakDataMapper mapper;

    @Override
    public void createRealm(String realmName) {
        createRealm(realmName, Collections.emptyList());
    }

    @Override
    public void createRealm(String realmName, List<String> scopes) {
        RealmsResource realmsResource = keycloak.realms();
        List<RealmRepresentation> allRealms = realmsResource.findAll();
        if (allRealms.stream().map(RealmRepresentation::getRealm).noneMatch(realmName::equals)) {
            realmsResource.create(mapper.createRealmRepresentation(realmName));
            log.info("Realm [{}] was created", realmName);
        }

        ClientScopesResource clientScopesResource = keycloak.realm(realmName).clientScopes();
        List<ClientScopeRepresentation> allClientScopes = clientScopesResource.findAll();
        List<String> allClientScopeNames = allClientScopes.stream().map(ClientScopeRepresentation::getName).collect(Collectors.toList());
        scopes.forEach(s -> {
            if (!allClientScopeNames.contains(s)) {
                clientScopesResource.create(mapper.createClientScopeRepresentation(s));
                log.info("Client scope [{}] was added to realm [{}]", s, realmName);
            }
        });
    }

    @Override
    public void createRealmWithRolesAndScopes(String realm, List<String> realmRoles, List<String> clientScopes) {
        createRealm(realm, clientScopes);
        createRealmRoles(realm, realmRoles);
    }

    @Override
    public void createRealmRoles(String realm, List<String> realmRoles) {
        RolesResource rolesResource = keycloak.realms().realm(realm).roles();
        List<RoleRepresentation> roleRepresentationList = rolesResource.list();
        realmRoles.forEach(r -> {
            if (roleRepresentationList.stream().map(RoleRepresentation::getName).noneMatch(r::equals)) {
                rolesResource.create(mapper.createRoleRepresentation(r));
                log.info("Realm role {} was created in realm {}", r, realm);
            }
        });
    }

    @Override
    public void createClient(String realm, KeycloakClient client) {
        ClientsResource clientsResource = keycloak.realm(realm).clients();
        List<ClientRepresentation> allClients = clientsResource.findAll();
        if (allClients.stream().map(ClientRepresentation::getName).noneMatch(client.getClientId()::equals)) {
            clientsResource.create(mapper.createClientRepresentation(client));
            log.info("Client [{}] was created in realm [{}]", client.getClientId(), realm);
        }

        //// TODO: assign scopes
        if (CollectionUtils.isNotEmpty(client.getScopes())) {
            List<ClientRepresentation> byClientId = clientsResource.findByClientId(client.getClientId());
            if (CollectionUtils.isNotEmpty(byClientId)) {
                ClientRepresentation clientRepresentation = byClientId.get(0);
                clientRepresentation.getDefaultClientScopes().addAll(client.getScopes());
                clientsResource.get(clientRepresentation.getId()).update(clientRepresentation);
                log.info("Client  scopes [{}] were assigned to client [{}] in realm [{}]", client.getScopes(), client.getClientId(), realm);
            }
        }
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
        UsersResource usersResource = keycloak.realm(realm).users();
        Response response = usersResource.create(mapper.createUserRepresentation(user));
        if (HttpStatus.CREATED.value() == response.getStatus()) {
            String userId = CreatedResponseUtil.getCreatedId(response);
            log.info("User[login: {}] is created with id: ", userId);

            assignUserRoles(realm, userId, user.getRealmRoles());
        }
    }

    @Override
    public void updateUser(String realm, KeycloakUser user) {
        UsersResource usersResource = keycloak.realm(realm).users();
        List<UserRepresentation> search = usersResource.search(user.getLogin());
        if (CollectionUtils.isNotEmpty(search)) {
            String userId = search.get(0).getId();
            UserResource userResource = usersResource.get(userId);
            userResource.update(mapper.toUpdateUserPresentation(userResource.toRepresentation(), user));
            log.debug("User[login: {}] was updated in keycloak.", user.getLogin());

            assignUserRoles(realm, userId, user.getRealmRoles());
            return;
        }
        log.info("User[login: {}] was not found in keycloak.", user.getLogin());
    }

    @Override
    public void deleteUser(String realm, String login) {
        UsersResource usersResource = keycloak.realm(realm).users();
        List<UserRepresentation> search = usersResource.search(login);
        if (CollectionUtils.isNotEmpty(search)) {
            String userId = search.get(0).getId();
            usersResource.delete(userId);
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

    @Override
    public void resetPassword(String realm, String login, String password) {
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);

        UsersResource usersResource = keycloak.realm(realm).users();
        List<UserRepresentation> search = usersResource.search(login);
        if (CollectionUtils.isNotEmpty(search)) {
            String userId = search.get(0).getId();
            UserResource userResource = usersResource.get(userId);
            userResource.resetPassword(passwordCred);

            log.info("User[login: {}] was password reset.", login);
        }
        log.info("User[login: {}] was not found in keycloak.", login);
    }

    @Override
    public void assignRealmRoleToUser(String realm, String login, List<String> realmRoles) {
        UsersResource usersResource = keycloak.realm(realm).users();
        List<UserRepresentation> search = usersResource.search(login);
        if (CollectionUtils.isNotEmpty(search)) {
            String userId = search.get(0).getId();

            assignUserRoles(realm, userId, realmRoles);
            log.info("Realm roles [{}] were assigned to User[realm: {}, login: {}] ", realmRoles, realm, login);
            return;
        }
        log.info("User[login: {}] was not found in keycloak.", login);
    }

    @Override
    public void removeRealmRoleFromUser(String realm, String login, List<String> realmRoles) {
        UsersResource usersResource = keycloak.realm(realm).users();
        List<UserRepresentation> search = usersResource.search(login);
        if (CollectionUtils.isNotEmpty(search)) {
            String userId = search.get(0).getId();

            UserResource userResource = usersResource.get(userId);
            realmRoles.forEach(r -> userResource.roles().realmLevel().remove(Collections.singletonList(getRealmRole(realm, r))));
            log.info("Realm roles [{}] were assigned to User[realm: {}, login: {}] ", realmRoles, realm, login);
            return;
        }
        log.info("User[login: {}] was not found in keycloak.", login);
    }

    private RoleRepresentation getRealmRole(String realm, String realmRole) {
        return keycloak.realm(realm).roles().get(realmRole).toRepresentation();
    }

    private void assignUserRoles(String realm, String userId, List<String> realmRoles) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        realmRoles.forEach(r -> userResource.roles().realmLevel().add(Collections.singletonList(getRealmRole(realm, r))));
    }
}

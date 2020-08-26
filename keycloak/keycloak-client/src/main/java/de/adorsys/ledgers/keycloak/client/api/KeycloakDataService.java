package de.adorsys.ledgers.keycloak.client.api;

import de.adorsys.ledgers.keycloak.client.model.KeycloakClient;
import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;

import java.util.List;
import java.util.Optional;

public interface KeycloakDataService {

    void createRealm(String realm);

    void createRealm(String realm, List<String> scopes);

    void createRealmRoles(String realm, List<String> realmRoles);

    void createRealmWithRolesAndScopes(String realm, List<String> realmRoles, List<String> clientScopes);

    void createClient(String realm, KeycloakClient client);

    boolean clientExists(String realm, String clientId);

    Optional<KeycloakUser> getUser(String realm, String login);

    void createUser(String realm, KeycloakUser user);

    void updateUser(String realm, KeycloakUser user);

    void deleteUser(String realm, String login);

    boolean userExists(String realm, String login);

    void resetPassword(String realm, String login, String password);

    void assignRealmRoleToUser(String realm, String login, List<String> realmRoles);

    void removeRealmRoleFromUser(String realm, String login, List<String> realmRoles);
}

package de.adorsys.ledgers.keycloak.client.api;

import de.adorsys.ledgers.keycloak.client.model.KeycloakClient;
import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;

import java.util.Optional;

public interface KeycloakDataService {

    void createRealm(String realmName);

    void createClient(KeycloakClient client);

    boolean clientExists(String realm, String clientId);

    Optional<KeycloakUser> getUser(String realm, String login);

    void createUser(String realm, KeycloakUser user);

    void updateUser(String realm, KeycloakUser user);

    void deleteUser(String realm, String login);

    boolean userExists(String realm, String login);
}

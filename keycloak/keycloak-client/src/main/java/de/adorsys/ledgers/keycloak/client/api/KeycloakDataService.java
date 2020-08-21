package de.adorsys.ledgers.keycloak.client.api;

import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;

public interface KeycloakDataService {

//    void createRealm();

//    void createClient(String clientName, String clientSecret);

    void createUser(KeycloakUser user);

    void updateUser(KeycloakUser user);

    void deleteUser(String login);
}

package de.adorsys.ledgers.keycloak.client.impl;

import de.adorsys.ledgers.keycloak.client.api.KeycloakDataService;
import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;
import lombok.RequiredArgsConstructor;
//import org.keycloak.admin.client.Keycloak;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeycloakDataServiceImpl implements KeycloakDataService {
//    private final Keycloak keycloak;

    public void createRealm(String realmName) {
//        keycloak.realms().create(createRealmRepresentation(realmName));
    }

    public void createClient(String realm, String clientName, String clientSecret) {
//        keycloak.realm(realm).clients().create(createClientRepresentation(clientName, clientSecret));
    }

//    @Override
    public void createUser(String realm, String clientName, String clientSecret) {

    }

    @Override
    public void createUser(KeycloakUser user) {

    }

    @Override
    public void updateUser(KeycloakUser user) {

    }

    @Override
    public void deleteUser(String login) {

    }

//    private RealmRepresentation createRealmRepresentation(String realmName) {
//        RealmRepresentation rr = new RealmRepresentation();
//        rr.setRealm(realmName);
//        rr.setDisplayName(realmName);
//        rr.setEnabled(true);
//        return rr;
//    }
//
//    private ClientRepresentation createClientRepresentation(String clientName, String clientSecret) {
//        ClientRepresentation client = new ClientRepresentation();
//        client.setId(clientName);
//        client.setName(clientName);
//        client.setDirectAccessGrantsEnabled(true);
//        client.setSecret(clientSecret);
//        return client;
//    }
}

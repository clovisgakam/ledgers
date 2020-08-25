package de.adorsys.ledgers.keycloak.client.mapper;

import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper(componentModel = "spring")
public interface KeycloakDataMapper {

    default KeycloakUser toKeycloakUser(UserRepresentation userRepresentation) {
        return new KeycloakUser(
                userRepresentation.getUsername(),
                null,
                userRepresentation.isEnabled(),
                userRepresentation.getFirstName(),
                userRepresentation.getLastName(),
                userRepresentation.getEmail(),
                userRepresentation.isEmailVerified()
        );
    }

    default UserRepresentation toUpdateUserPresentation(UserRepresentation userRepresentation, KeycloakUser user) {
        // TODO: add fields to update if needed
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setEnabled(user.getEnabled());
        userRepresentation.setEmailVerified(user.getEmailVerified());
        return userRepresentation;
    }

    default RealmRepresentation createRealmRepresentation(String realmName) {
        RealmRepresentation rr = new RealmRepresentation();
        rr.setRealm(realmName);
        rr.setDisplayName(realmName);
        rr.setEnabled(true);
        return rr;
    }

    default ClientRepresentation createClientRepresentation(String clientName, String clientSecret) {
        ClientRepresentation client = new ClientRepresentation();
        client.setId(clientName);
        client.setName(clientName);
        client.setDirectAccessGrantsEnabled(true);
        client.setSecret(clientSecret);
        return client;
    }

    default UserRepresentation createUserRepresentation(KeycloakUser user) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(user.getLogin());
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setEnabled(user.getEnabled());
        userRepresentation.setClientRoles(new HashMap<>());
        userRepresentation.setEmailVerified(user.getEmailVerified());

        userRepresentation.setCredentials(new ArrayList<>());
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(user.getPassword());
        credential.setTemporary(false);

        userRepresentation.getCredentials().add(credential);
        return userRepresentation;
    }
}

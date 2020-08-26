package de.adorsys.ledgers.keycloak.client.model;

import lombok.Data;

import java.util.List;

@Data
public class KeycloakDefaultSchema {
    private final String realm;
    private final List<String> scopes;
    private final List<String> realmRoles;
    private final KeycloakClient client;
}

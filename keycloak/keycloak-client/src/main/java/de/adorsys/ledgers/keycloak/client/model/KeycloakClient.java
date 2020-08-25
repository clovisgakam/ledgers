package de.adorsys.ledgers.keycloak.client.model;

import lombok.Data;

@Data
public class KeycloakClient {
    private final String realm;
    private final String clientId;
    private final String clientSecret;
}

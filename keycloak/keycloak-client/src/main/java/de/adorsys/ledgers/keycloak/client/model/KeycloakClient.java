package de.adorsys.ledgers.keycloak.client.model;

import lombok.Data;

import java.util.List;

@Data
public class KeycloakClient {
    private final String clientId;
    private final String clientSecret;
    private final List<String> redirectUrls;
    private final List<String> scopes;
}

package de.adorsys.ledgers.keycloak.client.model;

import lombok.*;

@Data
public class KeycloakUser {
    private final String login;
    private final String password;
    private final Boolean enabled;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final Boolean emailVerified;
}

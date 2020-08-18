package de.adorsys.ledgers.keycloak.client.api;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;

public interface KeycloakTokenService {

    BearerTokenTO login(String login, String password);

    BearerTokenBO exchangeToken(String oldToken, Integer timeToLive);

    boolean validate(BearerTokenTO token);
}

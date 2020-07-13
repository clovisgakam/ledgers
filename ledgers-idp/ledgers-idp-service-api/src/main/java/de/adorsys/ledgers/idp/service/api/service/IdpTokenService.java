package de.adorsys.ledgers.idp.service.api.service;

import de.adorsys.ledgers.idp.core.domain.IdpToken;

public interface IdpTokenService {
    IdpToken validate(String token);

    IdpToken login(String login, String password);
}

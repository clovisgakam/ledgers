package de.adorsys.ledgers.idp.core.domain;

import lombok.Data;

@Data
public class IdpToken {
    private String access_token;
    private String token_type = "Bearer";
    private long expires_in;
    private String refresh_token;
    private IdpAccessToken accessTokenObject;
}

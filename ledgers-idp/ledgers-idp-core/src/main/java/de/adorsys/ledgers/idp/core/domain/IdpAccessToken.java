package de.adorsys.ledgers.idp.core.domain;

import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class IdpAccessToken {
    private String sub;
    private String jti;
    private String login;
    private IdpRole role;
    private Date iat;
    private Date exp;
    private Map<String, String> act = new HashMap<>();
    private String scaId;
    private String authorisationId;
    private TokenUsage tokenUsage;
}

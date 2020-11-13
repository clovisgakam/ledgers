package de.adorsys.ledgers.sca.domain.sca.message;

import lombok.Data;

import java.net.URI;

@Data
public class PushScaMessage extends ScaMessage {
    private URI url;
    private String httpMethod;
    private String userLogin;
}

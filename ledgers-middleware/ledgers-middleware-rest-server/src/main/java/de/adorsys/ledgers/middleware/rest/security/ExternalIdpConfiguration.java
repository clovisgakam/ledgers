package de.adorsys.ledgers.middleware.rest.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ledgers.gluu")
public class ExternalIdpConfiguration {
    private String clientId;
    private String clientSecret;
    private String clientAuthenticationMethod;
    private String scope;
    private String authorizationGrantType;
    private String authBasePath;
}

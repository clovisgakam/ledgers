package de.adorsys.ledgers.keycloak.client.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KeycloakPropertyConfig.class)
public class KeycloakConfig {

    @Bean
    Keycloak getKeycloakAdminClient(KeycloakPropertyConfig config) {
        return KeycloakBuilder.builder()
                       .serverUrl(config.getUrl())
                       .realm(config.getRealm())
                       .username(config.getUser())
                       .password(config.getPassword())
                       .clientId(config.getClient())
                       .build();
    }
}

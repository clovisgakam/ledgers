package de.adorsys.ledgers.keycloak.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public class KeycloakPropertyConfig {
    private static final String DEFAULT_ADMIN_REALM = "master";

    private static final String DEFAULT_ADMIN_CLIENT = "admin-cli";

    @Value("${auth-server-url:}")
    private String url;

    @Value("${admin.username:}")
    private String user;

    @Value("${admin.password:}")
    private String password;

    @Value("${resource:" + DEFAULT_ADMIN_CLIENT + "}")
    private String client;

    private String realm = DEFAULT_ADMIN_REALM;

    public String getUrl() {
        return url;
    }

    public KeycloakPropertyConfig setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUser() {
        return user;
    }

    public KeycloakPropertyConfig setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public KeycloakPropertyConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getClient() {
        return client;
    }

    public KeycloakPropertyConfig setClient(String client) {
        this.client = client;
        return this;
    }

    public String getRealm() {
        return realm;
    }

    public KeycloakPropertyConfig setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    @Override
    public String toString() {
        return "KeycloakConfig{" +
                       "url='" + url + '\'' +
                       ", user='" + user + '\'' +
                       ", password='" + password + '\'' +
                       ", client='" + client + '\'' +
                       ", realm='" + realm +
                       '}';
    }
}

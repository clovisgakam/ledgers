package de.adorsys.ledgers.idp.app.server.auth;

public class PermittedResources {

    protected static final String[] SWAGGER_WHITELIST = {
            "/swagger-resources",
            "/swagger-ui.html",
            "/v2/api-docs",
            "/webjars/**"
    };

    protected static final String[] INDEX_WHITELIST = {
            "/index.css",
            "/img/*",
            "/favicon.ico"
    };

    protected static final String[] APP_WHITELIST = {
            "/",
            "/user/login"
    };

    protected static final String[] CONSOLE_WHITELIST = {
            "/console/**"
    };

    protected static final String[] ACTUATOR_WHITELIST = {
            "/actuator/info",
            "/actuator/health"
    };

    private PermittedResources() {
    }
}

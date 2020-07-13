package de.adorsys.ledgers.idp.app.server.auth;

import de.adorsys.ledgers.idp.core.domain.IdpAccessToken;
import de.adorsys.ledgers.idp.core.domain.IdpAuthentication;
import de.adorsys.ledgers.idp.rest.server.resource.security.JWTAuthenticationFilter;
import de.adorsys.ledgers.idp.service.api.service.TokenAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.context.annotation.RequestScope;

import java.security.Principal;
import java.util.Optional;

import static de.adorsys.ledgers.idp.app.server.auth.PermittedResources.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final TokenAuthenticationService tokenAuthenticationService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests().antMatchers(SWAGGER_WHITELIST).permitAll()
                .and()
                .authorizeRequests().antMatchers(INDEX_WHITELIST).permitAll()
                .and()
                .authorizeRequests().antMatchers(APP_WHITELIST).permitAll()
                .and()
                .authorizeRequests().antMatchers(CONSOLE_WHITELIST).permitAll()
                .and()
                .authorizeRequests().antMatchers(ACTUATOR_WHITELIST).permitAll()
                .and()
                .cors()
                .and()
                .authorizeRequests().anyRequest().authenticated();
        http.addFilterBefore(new JWTAuthenticationFilter(tokenAuthenticationService), BasicAuthenticationFilter.class);
        http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.headers().frameOptions().disable();
    }

    @Bean
    @RequestScope
    public Principal getPrincipal() {
        return auth().orElse(null);
    }

    @Bean
    @RequestScope
    public IdpAccessToken getIdpAccessToken() {
        return auth().map(this::extractToken).orElse(null);
    }

    private static Optional<IdpAuthentication> auth() {
        return SecurityContextHolder.getContext() == null
                       ? Optional.empty()
                       : Optional.ofNullable((IdpAuthentication) SecurityContextHolder.getContext().getAuthentication());
    }

    private IdpAccessToken extractToken(IdpAuthentication authentication) {
        return authentication.getBearerToken().getAccessTokenObject();
    }
}

package de.adorsys.ledgers.idp.core.domain;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class IdpAuthentication extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = -778888356552035882L;

    public IdpAuthentication(Object principal, IdpToken credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    public IdpToken getBearerToken() {
        return (IdpToken) getCredentials();
    }
}

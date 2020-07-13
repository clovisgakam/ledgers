package de.adorsys.ledgers.idp.service.impl.service;


import de.adorsys.ledgers.idp.core.domain.IdpAccessToken;
import de.adorsys.ledgers.idp.core.domain.IdpAuthentication;
import de.adorsys.ledgers.idp.core.domain.IdpRole;
import de.adorsys.ledgers.idp.core.domain.IdpToken;
import de.adorsys.ledgers.idp.service.api.service.IdpTokenService;
import de.adorsys.ledgers.idp.service.api.service.TokenAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenAuthenticationServiceImpl implements TokenAuthenticationService {
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_KEY = "Authorization";

    private final IdpTokenService tokenService;

    public Authentication getAuthentication(HttpServletRequest request) {
        String headerValue = request.getHeader(HEADER_KEY);
        if (StringUtils.isBlank(headerValue)) {
            log.debug("Header value '{}' is blank.", HEADER_KEY);
            return null;
        }

        if (!StringUtils.startsWithIgnoreCase(headerValue, TOKEN_PREFIX)) {
            log.debug("Header value does not start with '{}'.", TOKEN_PREFIX);
            return null;
        }

        String accessToken = StringUtils.substringAfterLast(headerValue, " ");

        IdpToken bearerToken = tokenService.validate(accessToken);

        List<GrantedAuthority> authorities = new ArrayList<>();

        IdpAccessToken accessTokenTO = bearerToken.getAccessTokenObject();
        IdpRole role = accessTokenTO.getRole();
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }

        return new IdpAuthentication(accessTokenTO.getSub(), bearerToken, authorities);
    }
}

package de.adorsys.ledgers.keycloak.client.mapper;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeycloakAuthMapperTest {

    private final KeycloakAuthMapper mapper = Mappers.getMapper(KeycloakAuthMapper.class);

    @Test
    void toAccessToken() {
        RefreshableKeycloakSecurityContext context = getTokenContext();
        AccessTokenTO token = mapper.toAccessToken(context);
        assertEquals("tokenString", token.getAccessToken());
        assertEquals(new Date(61558779600000L), token.getExp());
        assertEquals(new Date(615586932L), token.getIat());
        assertEquals("id", token.getJti());
        assertEquals(new HashSet<>(Arrays.asList("profile", "openId")), token.getScopes());
        assertEquals("anton.brueckner", token.getLogin());
        assertEquals("subj", token.getSub());
        assertEquals(UserRoleTO.CUSTOMER, token.getRole());
    }

    private RefreshableKeycloakSecurityContext getTokenContext() {
        return new RefreshableKeycloakSecurityContext(null, null, "tokenString", getAccessToken(), null, null, null);
    }

    private AccessToken getAccessToken() {
        AccessToken token = new AccessToken();
        token.id("id");
        token.issuedAt(615586932);
        token.exp(61558779600000L);
        token.setScope("profile openId");
        token.setPreferredUsername("anton.brueckner");
        token.setSubject("subj");
        token.setRealmAccess(new AccessToken.Access().addRole("CUSTOMER"));

        return token;
    }

    @Test
    void toBearerBO() {
        AccessTokenResponse source = getTokenResponse();
        BearerTokenBO result = mapper.toBearerBO(source);
        assertEquals("tokenString", result.getAccess_token());
        assertEquals(1000, result.getExpires_in());
        assertEquals("Bearer", result.getToken_type());
        assertEquals(new HashSet<>(Arrays.asList("profile", "openId")), result.getScopes());
    }

    private AccessTokenResponse getTokenResponse() {
        AccessTokenResponse tokenResponse = new AccessTokenResponse();
        tokenResponse.setToken("tokenString");
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiresIn(1000);
        tokenResponse.setScope("profile openId");
        return tokenResponse;
    }
}
package mapper;

import de.adorsys.ledgers.keycloak.client.mapper.KeycloakAuthMapperImpl;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class KeycloakAuthMapperTest {

    private static final String SUBJECT = "Token subject";
    private static final String PREFERRED_USERNAME = "anton.brueckner";

    @InjectMocks
    private KeycloakAuthMapperImpl mapper;

    @Test
    void toBearer() {
        AccessTokenResponse tokenResponse = getKeycloakAccessTokenResponse();
        BearerTokenBO result = mapper.toBearerBO(tokenResponse);
        assertNotNull(result);
    }

    private AccessTokenResponse getKeycloakAccessTokenResponse() {
        AccessTokenResponse response = new AccessTokenResponse();
        response.setScope("scope");
        response.setExpiresIn(1000);
        response.setIdToken("idToken");
        response.setRefreshToken("refreshToken");
        response.setToken("token string");
        response.setTokenType("Bearer");
        return response;
    }
}
package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GluuAuthenticationService {
    private final GluuRestClient client;
    private final ExternalIdpConfiguration conf;
    private final MiddlewareUserManagementService userManagementService;

    public SCALoginResponseTO token(String login, String password) {
        UserTO user = userManagementService.findByUserLogin(login);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.put("grant_type", Collections.singletonList("password"));
        map.put("username", Collections.singletonList(login));
        map.put("password", Collections.singletonList(password));
        map.put("scope", Collections.singletonList("openid+profile"));
        Map<String, ?> gluuToken = client.token(getBasicParam(), map).getBody();
        BearerTokenTO bearer = toBearer(gluuToken, user);
        return authorizeResponse(bearer);
    }

    public BearerTokenTO verify(String token) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.put("token", Collections.singletonList(token));
        try {
            Map<String, ? super Object> gluuToken = client.validate(getBasicParam(), map).getBody();
            gluuToken.put("access_token", token);
            return toBearer(gluuToken, null);
        } catch (FeignException e) {
            return null;
        }
    }

    private String getBasicParam() {
        return "Basic " + Base64.getEncoder().encodeToString((conf.getClientId() + ":" + conf.getClientSecret()).getBytes());
    }

    private SCALoginResponseTO authorizeResponse(BearerTokenTO token) {
        SCALoginResponseTO response = new SCALoginResponseTO();
        response.setScaStatus(ScaStatusTO.EXEMPTED);
        response.setBearerToken(token);
        response.setScaId(token.getAccessTokenObject().getScaId());
        response.setExpiresInSeconds(token.getExpires_in());
        response.setStatusDate(LocalDateTime.now());
        response.setAuthorisationId(token.getAccessTokenObject().getAuthorisationId());
        return response;
    }

    private BearerTokenTO toBearer(Map<String, ?> gluuToken, UserTO user) {
        AccessTokenTO accessToken = new AccessTokenTO();
        user = Optional.ofNullable(user).orElseGet(() -> getUser(gluuToken));
        accessToken.setLogin(user.getLogin());
        accessToken.setRole(user.getUserRoles().iterator().next());
        accessToken.setTokenUsage(TokenUsageTO.DIRECT_ACCESS);
        accessToken.setSub(user.getId());
        return new BearerTokenTO((String) gluuToken.get("access_token"),
                                 (String) gluuToken.get("token_type"),
                                 Optional.ofNullable((Integer) gluuToken.get("expires_in")).orElse(0),
                                 null,
                                 accessToken);
    }

    private UserTO getUser(Map<String, ?> gluuToken) {
        String username = Optional.ofNullable(gluuToken.get("username").toString())
                                  .orElseThrow(() -> MiddlewareModuleException.builder()
                                                             .errorCode(MiddlewareErrorCode.NO_SUCH_ALGORITHM)
                                                             .devMsg("")
                                                             .build());
        return userManagementService.findByUserLogin(username);
    }
}

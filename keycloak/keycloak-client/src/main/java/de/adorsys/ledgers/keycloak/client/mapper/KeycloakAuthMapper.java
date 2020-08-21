package de.adorsys.ledgers.keycloak.client.mapper;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import org.apache.commons.collections4.CollectionUtils;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface KeycloakAuthMapper {

    @Mapping(target = "act", ignore = true)
    @Mapping(target = "scaId", ignore = true)
    @Mapping(target = "consent", ignore = true)
    @Mapping(target = "authorisationId", ignore = true)
    @Mapping(target = "iat", source = "source.token.issuedAt")
    @Mapping(target = "role", expression = "java(getLedgersUserRoles(source.getToken()))")
    @Mapping(target = "sub", source = "source.token.subject")
    @Mapping(target = "scopes", source = "source.token.scope")
    @Mapping(target = "login", source = "source.token.preferredUsername")
    @Mapping(target = "exp", source = "source.token.exp")
    @Mapping(target = "jti", source = "source.token.id")
    @Mapping(target = "accessToken", source = "source.tokenString")
    @Mapping(target = "tokenUsage", expression = "java(de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO.DIRECT_ACCESS)")
//TODO This is a stub!!!
    AccessTokenTO toAccessToken(RefreshableKeycloakSecurityContext source);

    @Mapping(target = "accessTokenObject", ignore = true)
    @Mapping(target = "token_type", source = "source.tokenType")
    @Mapping(target = "refresh_token", source = "source.refreshToken")
    @Mapping(target = "expires_in", source = "source.expiresIn")
    @Mapping(target = "access_token", source = "source.token")
    @Mapping(target = "scopes", source = "source.scope")
    BearerTokenBO toBearerBO(AccessTokenResponse source);

    default Set<String> toScopes(String scope) {
        return new HashSet<>(Arrays.asList(scope.split(" ")));
    }

    @Mapping(target = "access_token", source = "token")
    @Mapping(target = "expires_in", source = "expiresIn")
    @Mapping(target = "refresh_token", source = "refreshToken")
    @Mapping(target = "token_type", source = "tokenType")
    BearerTokenBO toBearerTokenBO(AccessTokenResponse source);

    default Date toDate(long source) {
        return new Date(source);
    }

    default UserRoleTO getLedgersUserRoles(AccessToken token) {
        Set<String> tokenizedRoles = Optional.ofNullable(token.getRealmAccess())
                                             .map(AccessToken.Access::getRoles)
                                             .orElseGet(Collections::emptySet);
        Collection<UserRoleBO> roles = CollectionUtils.intersection(
                tokenizedRoles
                        .stream()
                        .map(UserRoleBO::getByValue)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()), Arrays.asList(UserRoleBO.values())
        );

        return roles.isEmpty()
                       ? null
                       : UserRoleTO.getByValue(roles.iterator().next().toString()).orElse(null);

    }
}

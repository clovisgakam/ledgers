package de.adorsys.ledgers.keycloak.client.mapper;

import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KeycloakDataMapper {

    @Mapping(target = "username", source = "login")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "emailVerified", constant = "true")
//    @Mapping(target = "credential", constant = "true")
    UserRepresentation toUserRepresentation(KeycloakUser user);
}

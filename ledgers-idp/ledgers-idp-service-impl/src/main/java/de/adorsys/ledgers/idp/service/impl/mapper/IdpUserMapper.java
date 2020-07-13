package de.adorsys.ledgers.idp.service.impl.mapper;

import de.adorsys.ledgers.idp.repository.domain.UserEntity;
import de.adorsys.ledgers.idp.service.api.domain.IdpUser;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IdpUserMapper {
    IdpUser toIdpUser(UserEntity source);

    UserEntity toUserEntity(IdpUser source);

    List<IdpUser> toIdpUsers(List<UserEntity> source);
}

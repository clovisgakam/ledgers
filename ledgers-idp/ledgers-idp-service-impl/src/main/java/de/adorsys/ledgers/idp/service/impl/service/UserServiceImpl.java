package de.adorsys.ledgers.idp.service.impl.service;

import de.adorsys.ledgers.idp.core.domain.UserStatus;
import de.adorsys.ledgers.idp.repository.domain.UserEntity;
import de.adorsys.ledgers.idp.repository.repository.IdpUserRepository;
import de.adorsys.ledgers.idp.service.api.domain.IdpUser;
import de.adorsys.ledgers.idp.service.api.service.UserService;
import de.adorsys.ledgers.idp.service.impl.mapper.IdpUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final IdpUserRepository repository;
    private final IdpUserMapper userMapper;

    @Override
    public IdpUser getById(long userId) {
        return repository.findById(userId)
                       .map(userMapper::toIdpUser)
                       .orElseThrow(() -> new RuntimeException());
    }

    @Override
    public IdpUser findByLogin(String login) {
        return repository.findByLogin(login)
                       .map(userMapper::toIdpUser)
                       .orElseThrow(() -> new RuntimeException());
    }

    @Override
    public List<IdpUser> getAllUsers() {
        return userMapper.toIdpUsers(repository.findAll());
    }

    @Override
    @Transactional
    public IdpUser registerUser(IdpUser user) {
        UserEntity entity = repository.save(userMapper.toUserEntity(user));
        return userMapper.toIdpUser(entity);
    }

    @Override
    public IdpUser updateUser(IdpUser user) {
        if (!repository.existsById(user.getUserId())) {
            throw new RuntimeException(); //TODO Exception!!!
        }
        return registerUser(user);
    }

    @Override
    @Transactional
    public void updateStatus(long userId, UserStatus status) {
        UserEntity user = repository.findById(userId)
                                  .orElseThrow(() -> new RuntimeException());
        user.setUserStatus(status);
    }
}

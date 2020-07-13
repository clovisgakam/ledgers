package de.adorsys.ledgers.idp.service.api.domain;

import de.adorsys.ledgers.idp.core.domain.IdpRole;
import de.adorsys.ledgers.idp.core.domain.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IdpUser {
    private long userId;
    private String login;
    private String password;
    private IdpRole role;
    private UserStatus userStatus;
    private LocalDateTime created;
}

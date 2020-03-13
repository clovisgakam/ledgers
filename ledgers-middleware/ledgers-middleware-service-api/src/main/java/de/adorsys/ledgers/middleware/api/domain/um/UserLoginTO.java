package de.adorsys.ledgers.middleware.api.domain.um;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginTO {
    private BearerTokenTO bearerToken;
    private boolean defaultPassword;
}

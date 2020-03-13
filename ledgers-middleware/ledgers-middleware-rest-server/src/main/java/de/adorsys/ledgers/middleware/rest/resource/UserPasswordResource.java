package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.service.MiddlewareUserPasswordService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.security.ResetPassword;
import de.adorsys.ledgers.security.SendCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@MiddlewareUserResource
@RequiredArgsConstructor
@RequestMapping(UserPasswordRestAPI.BASE_PATH)
public class UserPasswordResource implements UserPasswordRestAPI {

    private final MiddlewareUserPasswordService middlewareUserPasswordService;

    @Override
    public ResponseEntity<SendCode> sendCode(ResetPassword resetPassword) {
        return ResponseEntity.ok(middlewareUserPasswordService.sendCode(resetPassword));
    }

    @Override
    public ResponseEntity<Void> updatePassword(ResetPassword resetPassword) {
        middlewareUserPasswordService.updatePassword(resetPassword);
        return ResponseEntity.accepted().build();
    }
}

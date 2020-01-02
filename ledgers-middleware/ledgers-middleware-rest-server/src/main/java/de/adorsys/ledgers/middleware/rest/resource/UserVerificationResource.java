package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.service.EmailVerificationService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(UserVerificationRestAPI.BASE_PATH)
@MiddlewareUserResource
public class UserVerificationResource implements UserVerificationRestAPI {
    private final EmailVerificationService emailVerificationService;
    private final ScaInfoHolder scaInfoHolder;

    @Override
    @PreAuthorize("tokenUsage('DIRECT_ACCESS')")
    public ResponseEntity<Boolean> sendEmailVerification() {
        return ResponseEntity.ok(emailVerificationService.sendVerificationToken(scaInfoHolder.getUserId()));
    }

    @Override
    public ResponseEntity<Boolean> confirmVerificationToken(String token) {
        return ResponseEntity.ok(emailVerificationService.confirmVerificationToken(token));
    }
}
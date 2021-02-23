package de.adorsys.ledgers.middleware.impl.config;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.middleware.impl.service.validation.PaymentValidatorChain;
import de.adorsys.ledgers.um.api.domain.UserBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentValidatorService {
    private final List<PaymentValidatorChain> validators;

    public void validate(PaymentBO payment, UserBO user) {
        validators.forEach(v -> v.check(payment, user));
    }
}

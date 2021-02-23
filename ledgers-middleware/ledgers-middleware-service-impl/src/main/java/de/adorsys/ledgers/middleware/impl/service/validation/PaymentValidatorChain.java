package de.adorsys.ledgers.middleware.impl.service.validation;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.um.api.domain.UserBO;

public abstract class PaymentValidatorChain {
    private PaymentValidatorChain next;

    public PaymentValidatorChain next(PaymentValidatorChain next) {
        this.next = next;
        return next;
    }

    public abstract void check(PaymentBO payment, UserBO user);

    protected void checkNext(PaymentBO payment, UserBO user) {
        if (next != null) {
            next.check(payment, user);
        }
    }
}

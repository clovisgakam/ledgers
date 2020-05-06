package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.PaymentTarget;
import de.adorsys.ledgers.deposit.db.repository.DepositAccountRepository;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentExecutionScheduler {
    private static final String SCHEDULER = "Scheduler";
    private final PaymentRepository paymentRepository;
    private final DepositAccountRepository accountRepository;

    private final PaymentExecutionService executionService;

    @Scheduled(initialDelayString = "${paymentScheduler.initialDelay}", fixedDelayString = "${paymentScheduler.delay}")
    public void scheduler() {
        log.info("Payment Execution Scheduler started at {}", LocalDateTime.now());
        List<Payment> payments = paymentRepository.getAllDuePayments();
        payments.forEach(this::executeIfNotBlocked);
    }

    private void executeIfNotBlocked(Payment payment) {
        boolean debtorIsBlocked = isDebtorAccountBlocked(payment.getAccountId());
        boolean creditorsAreBlocked = areTargetsBlocked(payment.getTargets());

        if (!debtorIsBlocked && !creditorsAreBlocked) {
            executionService.executePayment(payment, SCHEDULER);
        }
    }

    private boolean isDebtorAccountBlocked(String accountId) {
        return accountRepository.findById(accountId)
                       .map(this::isDepositAccountBlocked)
                       .orElse(false);
    }

    private boolean areTargetsBlocked(List<PaymentTarget> targets) {
        return targets.stream()
                       .map(PaymentTarget::getCreditorAccount)
                       .map(ar -> accountRepository.findByIbanAndCurrency(ar.getIban(), ar.getCurrency())
                                          .orElse(null))
                       .anyMatch(this::isDepositAccountBlocked);
    }

    private boolean isDepositAccountBlocked(DepositAccount depositAccount) {
        return Optional.ofNullable(depositAccount)
                       .map(da -> da.isBlocked() || da.isSystemBlocked())
                       .orElse(false);
    }
}

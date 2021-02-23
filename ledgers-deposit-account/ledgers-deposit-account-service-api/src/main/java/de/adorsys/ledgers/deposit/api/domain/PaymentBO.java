package de.adorsys.ledgers.deposit.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.deposit.api.domain.ExecutionRules.FOLLOWING;
import static de.adorsys.ledgers.deposit.api.domain.ExecutionRules.PRECEDING;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentBO {
    private String paymentId;
    private Boolean batchBookingPreferred;
    private LocalDate requestedExecutionDate;
    private LocalTime requestedExecutionTime;
    private PaymentTypeBO paymentType;
    private String paymentProduct;
    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private FrequencyCodeBO frequency;
    private Integer dayOfExecution; //Day here max 31
    private AccountReferenceBO debtorAccount;
    private String debtorName;
    private String debtorAgent;
    private TransactionStatusBO transactionStatus;
    private List<PaymentTargetBO> targets = new ArrayList<>();
    private String accountId;

    @JsonIgnore
    public boolean isValidAmount() {
        return targets.stream()
                       .map(PaymentTargetBO::getInstructedAmount)
                       .allMatch(a -> a.getAmount().compareTo(BigDecimal.ZERO) > 0
                                              && a.getAmount().scale() < 3);
    }

    @JsonIgnore
    public void updateDebtorAccountCurrency(Currency currency) {
        debtorAccount.setCurrency(currency);
    }

    @JsonIgnore
    public boolean isInvalidExecutionRule() {
        if (this.executionRule == null || this.executionRule.isBlank()) {
            return false;
        }
        return !this.executionRule.equals(PRECEDING) && !this.executionRule.equals(FOLLOWING);
    }

    @JsonIgnore
    public boolean isInvalidEndToEndIds(boolean allowSameIds) {
        return !allowSameIds && this.targets.stream()
                                        .map(PaymentTargetBO::getEndToEndIdentification)
                                        .collect(Collectors.toSet()).size() != this.targets.size();
    }

    @JsonIgnore
    public boolean isInvalidRequestedExecutionDateTime(boolean allowDatesInThePast) {
        boolean datePresent = this.requestedExecutionDate != null;
        if (allowDatesInThePast) {
            return false;
        }
        return datePresent && this.requestedExecutionDate.isBefore(LocalDate.now()) ||
                       this.requestedExecutionTime != null
                               && LocalDateTime.of(datePresent ? this.requestedExecutionDate : LocalDate.now(),
                                                   this.requestedExecutionTime).isBefore(LocalDateTime.now());
    }

    @JsonIgnore
    public boolean isInvalidStartDate(boolean allowDatesInThePast) {
        boolean presentStartDate = this.getStartDate() != null;
        if (allowDatesInThePast) {
            return !presentStartDate;
        }

        return presentStartDate && startDate.isBefore(LocalDate.now());
    }

    @JsonIgnore
    public boolean isInvalidEndDate() {
        return this.endDate != null && this.endDate.isBefore(this.startDate);
    }

    @JsonIgnore
    public boolean isInvalidStartingTransactionStatus() {
        return this.transactionStatus != null && this.transactionStatus != TransactionStatusBO.RCVD;
    }
}

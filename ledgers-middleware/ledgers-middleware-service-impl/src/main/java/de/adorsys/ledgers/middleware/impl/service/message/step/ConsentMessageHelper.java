package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import lombok.AllArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static de.adorsys.ledgers.um.api.domain.AisAccountAccessTypeBO.ALL_ACCOUNTS;
import static de.adorsys.ledgers.um.api.domain.AisAccountAccessTypeBO.ALL_ACCOUNTS_WITH_BALANCES;

@AllArgsConstructor
public class ConsentMessageHelper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");

    private final AisConsentBO consent;

    public String template() {
        checkNullConsent();
        // Deleting the consent of a TPP by replacing it with an empty consent.
        AisAccountAccessInfoBO access = consent.getAccess();
        if (access == null) {
            return String.format("No account access to tpp with id: %s", consent.getTppId());
        }
        return prepareTemplate(access).toString();
    }

    public String exemptedTemplate() {
        checkNullConsent();
        // Deleting the consent of a TPP by replacing it with an empty consent.
        AisAccountAccessInfoBO access = consent.getAccess();
        if (access == null) {
            return String.format("No account access to tpp with id: %s", consent.getTppId());
        }

        StringBuilder b = prepareTemplate(access);

        b.append("This access has been granted. No TAN entry needed.");
        return b.toString();
    }

    private StringBuilder prepareTemplate(AisAccountAccessInfoBO access) {
        var b = new StringBuilder(String.format("Account access for TPP with id %s:%n", consent.getTppId()));
        if (consent.getFrequencyPerDay() <= 1) {
            if (consent.isRecurringIndicator()) {
                b.append("- Up to to 1 access per day.\n");
            } else {
                b.append("- For one time access.\n");
            }
        } else {
            b.append(String.format("- Up to %s accesses per day.%n", consent.getFrequencyPerDay()));
        }
        if (consent.getValidUntil() != null) {
            b.append(String.format("- Access valid until %s.%n", formatter.format(consent.getValidUntil())));
        }
        b.append("Access to following accounts:\n");
        if (ALL_ACCOUNTS.equals(access.getAllPsd2())) {
            b.append("All payments accounts without balances.\n");
        } else if (ALL_ACCOUNTS_WITH_BALANCES.equals(access.getAllPsd2())) {
            b.append("All payments accounts with balances and transactions.\n");
        }
        if (ALL_ACCOUNTS.equals(access.getAvailableAccounts())) {
            b.append("All available accounts without balances.\n");
        } else if (ALL_ACCOUNTS_WITH_BALANCES.equals(access.getAvailableAccounts())) {
            b.append("All available accounts with balances and transactions.\n");
        }
        format(b, access.getAccounts(), "Without balances: %s.\n");
        format(b, access.getBalances(), "With balances: %s.\n");
        format(b, access.getTransactions(), "With balances and transactions: %s.\n");
        return b;
    }

    private void format(StringBuilder b, List<String> accounts, String templ) {
        if (accounts != null && !accounts.isEmpty()) {
            b.append(String.format(templ, accountList(accounts)));
        }
    }

    private String accountList(List<String> accounts) {
        var sb = new StringBuilder();
        accounts.forEach(a -> sb.append(a).append(" "));
        return sb.toString();
    }

    private void checkNullConsent() { //TODO Get rid of internal validations! Should be done on request level!
        if(consent==null) {
            throw new IllegalStateException("Not expecting consent to be null.");
        }
        if(consent.getTppId()==null) {
            throw new IllegalStateException("Not expecting tppId to be null.");
        }
    }
}

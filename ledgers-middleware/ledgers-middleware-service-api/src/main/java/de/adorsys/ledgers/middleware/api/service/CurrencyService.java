package de.adorsys.ledgers.middleware.api.service;

import java.util.Currency;
import java.util.List;

public interface CurrencyService {
    /**
     * @return list of supported currencies
     */
    List<Currency> getSupportedCurrencies();

    /**
     * Check if currency is supported
     *
     * @param currency currency used
     */
    void checkCurrencies(Currency currency);
}

package de.adorsys.ledgers.middleware.api.service;

import java.util.Currency;
import java.util.List;

public interface CurrencyService {
    /**
     * @return list of supported currencies
     */
    List<Currency> getSupportedCurrencies();
}

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.service.CurrencyService;
import de.adorsys.ledgers.middleware.impl.config.CurrencyConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {
    private final CurrencyConfigurationProperties currencyConfigProp;

    @Override
    public List<Currency> getSupportedCurrencies() {
        return currencyConfigProp.getCurrencies();
    }

    @Override
    public void checkCurrencies(Currency currency) {
        if (!getSupportedCurrencies().contains(currency)) {
            throw new IllegalArgumentException("Currency is not supported: " + currency);
        }
    }
}

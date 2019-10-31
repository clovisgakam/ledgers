package de.adorsys.ledgers.middleware.impl.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Getter
@Configuration
@ConfigurationProperties(prefix = "currency")
public class CurrencyConfigurationProperties {
    private List<Currency> currencies = new ArrayList<>();

}

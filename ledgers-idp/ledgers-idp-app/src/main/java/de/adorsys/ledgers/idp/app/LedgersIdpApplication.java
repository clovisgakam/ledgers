package de.adorsys.ledgers.idp.app;

import de.adorsys.ledgers.idp.repository.EnableIdpRepository;
import de.adorsys.ledgers.idp.rest.server.annotation.EnableIdpRest;
import de.adorsys.ledgers.idp.service.impl.annotation.EnableIdpService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@EnableIdpRepository
@EnableIdpRest
@EnableIdpService
@SpringBootApplication
public class LedgersIdpApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(LedgersIdpApplication.class).run(args);
    }
}

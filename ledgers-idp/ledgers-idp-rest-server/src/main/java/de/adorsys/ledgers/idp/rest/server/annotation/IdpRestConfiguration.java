package de.adorsys.ledgers.idp.rest.server.annotation;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages= {"de.adorsys.ledgers.idp.rest.server"})
public class IdpRestConfiguration {
}

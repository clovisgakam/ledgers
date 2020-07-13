package de.adorsys.ledgers.idp.app.server;

import de.adorsys.ledgers.idp.rest.server.annotation.IdpResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;

import static springfox.documentation.swagger.web.SecurityConfigurationBuilder.builder;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    private static final String API_KEY = "apiKey";

    @Bean
    public Docket apiDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                       .groupName("Ledgers IDP API")
                       .apiInfo(apiInfo())
                       .select()
                       .apis(RequestHandlerSelectors.withClassAnnotation(IdpResource.class))
                       .paths(PathSelectors.any())
                       .build()
                       .pathMapping("/")
                       .securitySchemes(Collections.singletonList(apiKey()))
                       .securityContexts(Collections.singletonList(securityContext()));
    }

    @Bean
    public SecurityConfiguration security() {
        return builder()
                       .clientId(null)
                       .clientSecret(null)
                       .realm(null)
                       .appName(null)
                       .scopeSeparator(",")
                       .useBasicAuthenticationWithAccessCodeGrant(false)
                       .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                       .title("Adorsys Ledgers IDP App")
                       .license("Apache License Version 2.0")
                       .version("1.0")
                       .build();
    }

    private ApiKey apiKey() {
        return new ApiKey("apiKey", "Authorization", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                       .securityReferences(defaultAuth())
                       .forPaths(PathSelectors.regex("/*"))
                       .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(new SecurityReference(API_KEY, authorizationScopes));
    }
}

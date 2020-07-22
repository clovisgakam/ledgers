package de.adorsys.ledgers.middleware.rest.security;

import feign.Headers;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = "gluuRestClien", url = "${ledgers.gluu.auth-base-path")
public interface GluuRestClient {

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    Response token(@RequestHeader("Authorization") String authorization, Map<String, ?> formParams);

    @PostMapping(value = "/introspection", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    Response validate(@RequestHeader("Authorization") String authorization, Map<String, ?> formParams);
}

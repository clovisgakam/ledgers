package de.adorsys.ledgers.middleware.rest.security;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = "gluuRestClien", url = "${ledgers.gluu.auth-base-path}", configuration = CustomFeignConfiguration.class)
public interface GluuRestClient {

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    ResponseEntity<Map<String, ?>> token(@RequestHeader("Authorization") String authorization, Map<String, ?> formParams);

    @PostMapping(value = "/introspection", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    ResponseEntity<Map<String, ? super Object>> validate(@RequestHeader("Authorization") String authorization, Map<String, ?> formParams);
}

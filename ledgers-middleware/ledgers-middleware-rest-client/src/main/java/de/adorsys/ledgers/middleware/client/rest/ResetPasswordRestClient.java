package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.UserPasswordRestAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "resetPassword", url = LedgersURL.LEDGERS_URL, path = UserPasswordRestAPI.BASE_PATH)
public interface ResetPasswordRestClient extends UserPasswordRestAPI {
}

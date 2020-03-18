package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.UserManagementAdminRestAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "ledgersUserMgmtAdmin", url = LedgersURL.LEDGERS_URL, path = UserManagementAdminRestAPI.BASE_PATH)
public interface UserManagementAdminRestClient extends UserManagementAdminRestAPI {
}

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.security.ResetPassword;
import de.adorsys.ledgers.security.SendCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "LDG007 - Reset user password")
public interface UserPasswordRestAPI {
    String BASE_PATH = "/password";

    @PostMapping
    @ApiOperation(value = "Send code for user password reset")
    ResponseEntity<SendCode> sendCode(@RequestBody ResetPassword resetPassword);

    @PutMapping
    @ApiOperation(value = "Update user password")
    ResponseEntity<Void> updatePassword(@RequestBody ResetPassword resetPassword);
}

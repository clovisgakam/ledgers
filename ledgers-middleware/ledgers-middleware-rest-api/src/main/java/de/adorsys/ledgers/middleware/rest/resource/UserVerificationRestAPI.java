package de.adorsys.ledgers.middleware.rest.resource;

import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "LDG013 - User verification", description = "Provides endpoint for sending verification email and user confirmation.")
public interface UserVerificationRestAPI {
    String BASE_PATH = "/emails";

    @PostMapping("/email-verification")
    @ApiOperation(value = "Send email for user verification", authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Boolean.class, message = "Email for user verification was successfully sent."),
            @ApiResponse(code = 400, message = "Error sending email: user not found."),
            @ApiResponse(code = 404, message = "Error sending email: verification token not found.")
    })
    ResponseEntity<Void> sendEmailVerification();

    @GetMapping("/email")
    @ApiOperation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Confirm user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Boolean.class, message = "User was successfully confirm."),
            @ApiResponse(code = 400, message = "Invalid verification token for user confirmation or user already confirm."),
            @ApiResponse(code = 403, message = "Verification token is expired for user confirmation.")
    })
    ResponseEntity<Void> confirmVerificationToken(@RequestParam("verificationToken") String token);
}
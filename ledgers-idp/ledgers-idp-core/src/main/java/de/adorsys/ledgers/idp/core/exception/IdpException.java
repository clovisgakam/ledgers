package de.adorsys.ledgers.idp.core.exception;

import lombok.Builder;
import lombok.Data;

import java.util.function.Supplier;

import static java.lang.String.format;

@Data
@Builder
public class IdpException extends RuntimeException {
    private final IdpErrorCode errorCode;
    private final String devMsg;

    public static Supplier<IdpException> blockedSupplier(IdpErrorCode code, String login) {
        return () -> IdpException.builder()
                             .errorCode(code)
                             .devMsg(format("User: %s is %s", login, "BLOCKED"))
                             .build();
    }

    public static Supplier<IdpException> tokenSupplier(IdpErrorCode code) {
        return () -> IdpException.builder()
                             .errorCode(code)
                             .devMsg("INVALIIID   %s!!!!!")
                             .build();
    }
}

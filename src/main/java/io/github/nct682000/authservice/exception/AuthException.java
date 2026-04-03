package io.github.nct682000.authservice.exception;

import io.github.nct682000.authservice.enumeration.ResponseCode;
import lombok.Getter;

@Getter
public class AuthException extends Exception {

    private final ResponseCode responseCode;

    public AuthException(ResponseCode responseCode) {
        super(responseCode.getMean());
        this.responseCode = responseCode;
    }

    public AuthException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

}

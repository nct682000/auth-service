package io.github.nct682000.authservice.exception;

import io.github.nct682000.authservice.enumeration.ResponseCode;

public class InvalidTokenException extends AuthException {

    public InvalidTokenException() {
        super(ResponseCode.TOKEN_INVALID);
    }
}

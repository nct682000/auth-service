package io.github.nct682000.authservice.exception;

import io.github.nct682000.authservice.enumeration.ResponseCode;

public class InvalidPasswordException extends AuthException {

    public InvalidPasswordException() {
        super(ResponseCode.INVALID_CREDENTIALS);
    }
}

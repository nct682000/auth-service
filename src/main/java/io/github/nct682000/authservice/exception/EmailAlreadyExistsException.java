package io.github.nct682000.authservice.exception;

import io.github.nct682000.authservice.enumeration.ResponseCode;

public class EmailAlreadyExistsException extends AuthException {

    public EmailAlreadyExistsException() {
        super(ResponseCode.EMAIL_ALREADY_EXISTS);
    }
}

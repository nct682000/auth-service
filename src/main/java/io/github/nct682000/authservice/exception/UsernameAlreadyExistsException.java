package io.github.nct682000.authservice.exception;

import io.github.nct682000.authservice.enumeration.ResponseCode;

public class UsernameAlreadyExistsException extends AuthException {

    public UsernameAlreadyExistsException() {
        super(ResponseCode.USERNAME_ALREADY_EXISTS);
    }
}

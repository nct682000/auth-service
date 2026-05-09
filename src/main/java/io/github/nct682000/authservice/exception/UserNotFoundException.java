package io.github.nct682000.authservice.exception;

import io.github.nct682000.authservice.enumeration.ResponseCode;

public class UserNotFoundException extends AuthException {

    public UserNotFoundException(String identifier) {
        super(ResponseCode.USER_NOT_FOUND, "User not found: " + identifier);
    }
}

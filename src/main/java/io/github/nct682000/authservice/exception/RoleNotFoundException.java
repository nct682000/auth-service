package io.github.nct682000.authservice.exception;

import io.github.nct682000.authservice.enumeration.ResponseCode;

public class RoleNotFoundException extends AuthException {

    public RoleNotFoundException(String identifier) {
        super(ResponseCode.ROLE_NOT_FOUND, "Required role not found: " + identifier);
    }
}

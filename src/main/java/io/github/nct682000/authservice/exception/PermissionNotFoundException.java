package io.github.nct682000.authservice.exception;

import io.github.nct682000.authservice.enumeration.ResponseCode;

public class PermissionNotFoundException extends AuthException {
    public PermissionNotFoundException(String identifier) {
        super(ResponseCode.PERMISSION_NOT_FOUND, "Permission not found: " + identifier);
    }
}

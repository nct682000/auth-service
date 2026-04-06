package io.github.nct682000.authservice.exception;

import io.github.nct682000.authservice.enumeration.ResponseCode;

public class RevokedTokenException extends AuthException {

    public RevokedTokenException() {
        super(ResponseCode.TOKEN_REVOKED);
    }
}

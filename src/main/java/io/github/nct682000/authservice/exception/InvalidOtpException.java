package io.github.nct682000.authservice.exception;

import io.github.nct682000.authservice.enumeration.ResponseCode;

public class InvalidOtpException extends AuthException {

    public InvalidOtpException() {
        super(ResponseCode.OTP_INVALID);
    }
}

package io.github.nct682000.authservice.enumeration;

import org.springframework.http.HttpStatus;

public enum ResponseCode {

    // ===== Generic =====
    INTERNAL_SERVER_ERROR("AUTH-000-500", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("AUTH-001-400", "Validation error", HttpStatus.BAD_REQUEST),

    // ===== Auth — success =====
    LOGIN_SUCCESS("AUTH-002-200", "Login successful", HttpStatus.OK),
    REGISTER_SUCCESS("AUTH-003-201", "Registration successful", HttpStatus.CREATED),
    LOGOUT_SUCCESS("AUTH-004-200", "Logout successful", HttpStatus.OK),
    TOKEN_REFRESHED("AUTH-005-200", "Token refreshed", HttpStatus.OK),
    PASSWORD_CHANGED("AUTH-006-200", "Password changed successfully", HttpStatus.OK),
    PASSWORD_RESET_OTP_SENT("AUTH-007-200", "OTP sent to your email", HttpStatus.OK),
    PASSWORD_RESET_SUCCESS("AUTH-008-200", "Password reset successfully", HttpStatus.OK),
    GET_PROFILE_SUCCESS("AUTH-025-200", "User profile retrieved successfully", HttpStatus.OK),

    // ===== Auth — failure =====
    INVALID_CREDENTIALS("AUTH-009-401", "Invalid username or password", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("AUTH-010-423", "Account is locked", HttpStatus.LOCKED),
    ACCOUNT_DISABLED("AUTH-011-403", "Account is disabled", HttpStatus.FORBIDDEN),
    ACCOUNT_EXPIRED("AUTH-012-401", "Account has expired", HttpStatus.UNAUTHORIZED),
    CREDENTIALS_EXPIRED("AUTH-013-401", "Credentials have expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("AUTH-014-401", "Token is invalid", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("AUTH-015-401", "Token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED("AUTH-016-401", "Token has been revoked", HttpStatus.UNAUTHORIZED),

    // ===== Conflict =====
    USERNAME_ALREADY_EXISTS("AUTH-017-409", "Username is already taken", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS("AUTH-018-409", "Email is already registered", HttpStatus.CONFLICT),

    // ===== Not found =====
    USER_NOT_FOUND("AUTH-019-404", "User not found", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND("AUTH-020-500", "Required role not found — check seed data", HttpStatus.INTERNAL_SERVER_ERROR),

    // ===== Authorization =====
    PERMISSION_DENIED("AUTH-021-403", "Access denied", HttpStatus.FORBIDDEN),

    // ===== OTP =====
    OTP_INVALID("AUTH-022-400", "OTP is invalid", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED("AUTH-023-400", "OTP has expired", HttpStatus.BAD_REQUEST),

    // ===== Rate limiting =====
    RATE_LIMIT_EXCEEDED("AUTH-024-429", "Too many requests — please try again later", HttpStatus.TOO_MANY_REQUESTS),
    ;

    private final String code;
    private final String mean;
    private final HttpStatus httpStatus;

    ResponseCode(String code, String mean, HttpStatus httpStatus) {
        this.code = code;
        this.mean = mean;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMean() {
        return mean;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

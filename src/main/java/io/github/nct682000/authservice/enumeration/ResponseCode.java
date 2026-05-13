package io.github.nct682000.authservice.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {

    // ===== Generic =====
    INTERNAL_SERVER_ERROR("AUTH-000-500", "auth.error.internal", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("AUTH-001-400", "auth.error.validation", HttpStatus.BAD_REQUEST),

    // ===== Auth - success =====
    LOGIN_SUCCESS("AUTH-002-200", "auth.success.login", HttpStatus.OK),
    REGISTER_SUCCESS("AUTH-003-201", "auth.success.register", HttpStatus.CREATED),
    LOGOUT_SUCCESS("AUTH-004-200", "auth.success.logout", HttpStatus.OK),
    TOKEN_REFRESHED("AUTH-005-200", "auth.success.token.refreshed", HttpStatus.OK),
    PASSWORD_CHANGED("AUTH-006-200", "auth.success.password.changed", HttpStatus.OK),
    PASSWORD_RESET_OTP_SENT("AUTH-007-200", "auth.success.password.reset.otp.sent", HttpStatus.OK),
    PASSWORD_RESET_SUCCESS("AUTH-008-200", "auth.success.password.reset", HttpStatus.OK),
    GET_PROFILE_SUCCESS("AUTH-025-200", "auth.success.profile.get", HttpStatus.OK),

    // ===== Auth - failure =====
    INVALID_CREDENTIALS("AUTH-009-401", "auth.error.invalid.credentials", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("AUTH-010-423", "auth.error.account.locked", HttpStatus.LOCKED),
    ACCOUNT_DISABLED("AUTH-011-403", "auth.error.account.disabled", HttpStatus.FORBIDDEN),
    ACCOUNT_EXPIRED("AUTH-012-401", "auth.error.account.expired", HttpStatus.UNAUTHORIZED),
    CREDENTIALS_EXPIRED("AUTH-013-401", "auth.error.credentials.expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("AUTH-014-401", "auth.error.token.invalid", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("AUTH-015-401", "auth.error.token.expired", HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED("AUTH-016-401", "auth.error.token.revoked", HttpStatus.UNAUTHORIZED),

    // ===== Conflict =====
    USERNAME_ALREADY_EXISTS("AUTH-017-409", "auth.error.username.exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS("AUTH-018-409", "auth.error.email.exists", HttpStatus.CONFLICT),

    // ===== Not found =====
    USER_NOT_FOUND("AUTH-019-404", "auth.error.user.not.found", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND("AUTH-020-500", "auth.error.role.not.found", HttpStatus.INTERNAL_SERVER_ERROR),

    // ===== Authorization =====
    PERMISSION_DENIED("AUTH-021-403", "auth.error.permission.denied", HttpStatus.FORBIDDEN),

    // ===== OTP =====
    OTP_INVALID("AUTH-022-400", "auth.error.otp.invalid", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED("AUTH-023-400", "auth.error.otp.expired", HttpStatus.BAD_REQUEST),

    // ===== Password lifecycle =====
    PASSWORD_CONFIRMATION_MISMATCH("AUTH-038-400", "auth.error.password.confirmation.mismatch", HttpStatus.BAD_REQUEST),
    PASSWORD_SAME_AS_CURRENT("AUTH-039-400", "auth.error.password.same.as.current", HttpStatus.BAD_REQUEST),

    // ===== Rate limiting =====
    RATE_LIMIT_EXCEEDED("AUTH-024-429", "auth.error.rate.limit", HttpStatus.TOO_MANY_REQUESTS),

    // ===== Admin - user management =====
    USER_STATUS_UPDATED("AUTH-026-200", "auth.success.admin.user.status.updated", HttpStatus.OK),
    ROLE_ASSIGNED("AUTH-027-200", "auth.success.admin.role.assigned", HttpStatus.OK),
    ROLE_REMOVED("AUTH-028-200", "auth.success.admin.role.removed", HttpStatus.OK),

    // ===== Admin - role management =====
    ROLE_CREATED("AUTH-029-201", "auth.success.admin.role.created", HttpStatus.CREATED),
    ROLE_DELETED("AUTH-030-200", "auth.success.admin.role.deleted", HttpStatus.OK),
    ROLE_ALREADY_EXISTS("AUTH-031-409", "auth.error.admin.role.exists", HttpStatus.CONFLICT),
    PERMISSION_ASSIGNED("AUTH-032-200", "auth.success.admin.permission.assigned", HttpStatus.OK),
    PERMISSION_REMOVED("AUTH-033-200", "auth.success.admin.permission.removed", HttpStatus.OK),

    // ===== Admin - permission management =====
    PERMISSION_CREATED("AUTH-034-201", "auth.success.admin.permission.created", HttpStatus.CREATED),
    PERMISSION_DELETED("AUTH-035-200", "auth.success.admin.permission.deleted", HttpStatus.OK),
    PERMISSION_ALREADY_EXISTS("AUTH-036-409", "auth.error.admin.permission.exists", HttpStatus.CONFLICT),
    PERMISSION_NOT_FOUND("AUTH-037-404", "auth.error.admin.permission.not.found", HttpStatus.NOT_FOUND),
    ;

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;

    ResponseCode(String code, String messageKey, HttpStatus httpStatus) {
        this.code = code;
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
    }

}

package io.github.nct682000.authservice.handler;

import io.github.nct682000.authservice.dto.APIResponse;
import io.github.nct682000.authservice.enumeration.ResponseCode;
import io.github.nct682000.authservice.exception.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Invalid request");

        log.warn("Validation failed: {}", message);
        return buildErrorResponse(ResponseCode.VALIDATION_ERROR.getCode(), message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<APIResponse<?>> handleAuthException(AuthException ex) {
        log.warn("Auth error [{}]: {}", ex.getResponseCode(), ex.getMessage());
        return buildErrorResponse(ex.getResponseCode().getCode(), ex.getMessage(), ex.getResponseCode().getHttpStatus());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<APIResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.INVALID_CREDENTIALS.getCode(), ResponseCode.INVALID_CREDENTIALS.getMean(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<APIResponse<?>> handleLocked(LockedException ex) {
        log.warn("Account locked: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.ACCOUNT_LOCKED.getCode(), ResponseCode.ACCOUNT_LOCKED.getMean(), HttpStatus.LOCKED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<APIResponse<?>> handleDisabled(DisabledException ex) {
        log.warn("Account disabled: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.ACCOUNT_DISABLED.getCode(), ResponseCode.ACCOUNT_DISABLED.getMean(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccountExpiredException.class)
    public ResponseEntity<APIResponse<?>> handleAccountExpired(AccountExpiredException ex) {
        log.warn("Account expired: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.ACCOUNT_EXPIRED.getCode(), ResponseCode.ACCOUNT_EXPIRED.getMean(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<APIResponse<?>> handleCredentialsExpired(CredentialsExpiredException ex) {
        log.warn("Credentials expired: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.CREDENTIALS_EXPIRED.getCode(), ResponseCode.CREDENTIALS_EXPIRED.getMean(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<APIResponse<?>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.INVALID_CREDENTIALS.getCode(), ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.PERMISSION_DENIED.getCode(), ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<?>> handleException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildErrorResponse(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<APIResponse<?>> buildErrorResponse(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(APIResponse.builder()
                        .code(code)
                        .message(message)
                        .build());
    }
}

package io.github.nct682000.authservice.handler;

import io.github.nct682000.authservice.dto.APIResponse;
import io.github.nct682000.authservice.enumeration.ResponseCode;
import io.github.nct682000.authservice.exception.AuthException;
import io.github.nct682000.authservice.service.MessageResolver;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageResolver messageResolver;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElseGet(() -> messageResolver.resolve(ResponseCode.VALIDATION_ERROR));

        log.info("Validation failed [400]: {}", message);
        return buildErrorResponse(ResponseCode.VALIDATION_ERROR.getCode(), message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<APIResponse<?>> handleAuthException(AuthException ex) {
        ResponseCode code = ex.getResponseCode();
        log.info("Auth error [{}]: {}", code.getCode(), ex.getMessage());
        return buildErrorResponse(code.getCode(), messageResolver.resolve(code), code.getHttpStatus());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<APIResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        log.info("Bad credentials [401]: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<APIResponse<?>> handleLocked(LockedException ex) {
        log.info("Account locked [423]: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.ACCOUNT_LOCKED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<APIResponse<?>> handleDisabled(DisabledException ex) {
        log.info("Account disabled [403]: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.ACCOUNT_DISABLED);
    }

    @ExceptionHandler(AccountExpiredException.class)
    public ResponseEntity<APIResponse<?>> handleAccountExpired(AccountExpiredException ex) {
        log.info("Account expired [401]: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.ACCOUNT_EXPIRED);
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<APIResponse<?>> handleCredentialsExpired(CredentialsExpiredException ex) {
        log.info("Credentials expired [401]: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.CREDENTIALS_EXPIRED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<APIResponse<?>> handleAuthenticationException(AuthenticationException ex) {
        log.info("Authentication failed [401]: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        log.info("Access denied [403]: {}", ex.getMessage());
        return buildErrorResponse(ResponseCode.PERMISSION_DENIED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<?>> handleException(Exception ex) {
        log.error("Unexpected error [500]: {}", ex.getMessage(), ex);
        return buildErrorResponse(ResponseCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<APIResponse<?>> buildErrorResponse(ResponseCode code) {
        return buildErrorResponse(code.getCode(), messageResolver.resolve(code), code.getHttpStatus());
    }

    private ResponseEntity<APIResponse<?>> buildErrorResponse(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(APIResponse.builder()
                        .code(code)
                        .message(message)
                        .build());
    }
}

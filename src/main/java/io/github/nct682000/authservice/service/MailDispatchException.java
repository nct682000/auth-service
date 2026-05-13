package io.github.nct682000.authservice.service;

public class MailDispatchException extends RuntimeException {

    public MailDispatchException(Throwable cause) {
        super("Failed to dispatch mail", cause);
    }

    public MailDispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}

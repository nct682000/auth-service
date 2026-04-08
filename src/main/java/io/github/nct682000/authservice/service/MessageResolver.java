package io.github.nct682000.authservice.service;

import io.github.nct682000.authservice.enumeration.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around {@link MessageSource} that reads the current locale from
 * {@link LocaleContextHolder} — which Spring MVC populates from the
 * {@code Accept-Language} header before every controller invocation.
 *
 * <p>Use this in controllers and {@code @RestControllerAdvice} handlers.
 * For security filter handlers (which run before Spring MVC sets the locale),
 * inject {@link MessageSource} directly and resolve the locale from
 * {@code HttpServletRequest#getLocale()}.
 */
@Component
@RequiredArgsConstructor
public class MessageResolver {

    private final MessageSource messageSource;

    public String resolve(ResponseCode code) {
        return messageSource.getMessage(code.getMessageKey(), null, LocaleContextHolder.getLocale());
    }

    public String resolve(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}

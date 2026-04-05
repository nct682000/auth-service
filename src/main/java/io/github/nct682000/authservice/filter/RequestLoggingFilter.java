package io.github.nct682000.authservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Request-ID";
    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {

        String traceId = Optional.ofNullable(request.getHeader(TRACE_ID_HEADER))
                .filter(h -> !h.isBlank())
                .orElse(UUID.randomUUID().toString());

        MDC.put(TRACE_ID_MDC_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String clientIp = resolveClientIp(request);
        long startTime = System.currentTimeMillis();

        log.info("START [{} {}] ip={}", method, uri, clientIp);

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("END   [{} {}] status={} duration={}ms", method, uri, response.getStatus(), duration);
            MDC.clear();
        }
    }

    /**
     * Resolves the real client IP.
     * In K8s/behind a proxy, the actual IP is in X-Forwarded-For.
     * We take the first entry (original client), not the proxy chain.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

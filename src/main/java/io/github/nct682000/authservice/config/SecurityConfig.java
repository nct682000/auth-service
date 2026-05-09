package io.github.nct682000.authservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.nct682000.authservice.dto.APIResponse;
import io.github.nct682000.authservice.enumeration.ResponseCode;
import io.github.nct682000.authservice.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/register",
            "/login",
            "/refresh",
            "/forgot-password",
            "/reset-password",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Security filter handlers run before Spring MVC sets LocaleContextHolder,
     * so we resolve the locale directly from the servlet request (which reads
     * the Accept-Language header per the servlet spec).
     */
    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) -> {
            String message = messageSource.getMessage(
                    ResponseCode.TOKEN_INVALID.getMessageKey(), null, request.getLocale());
            APIResponse<?> body = APIResponse.builder()
                    .code(ResponseCode.TOKEN_INVALID.getCode())
                    .message(message)
                    .build();
            writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, body);
        };
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            String message = messageSource.getMessage(
                    ResponseCode.PERMISSION_DENIED.getMessageKey(), null, request.getLocale());
            APIResponse<?> body = APIResponse.builder()
                    .code(ResponseCode.PERMISSION_DENIED.getCode())
                    .message(message)
                    .build();
            writeResponse(response, HttpServletResponse.SC_FORBIDDEN, body);
        };
    }

    private void writeResponse(HttpServletResponse response, int status, APIResponse<?> body)
            throws IOException {
        byte[] bytes = objectMapper.writeValueAsBytes(body);
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }
}

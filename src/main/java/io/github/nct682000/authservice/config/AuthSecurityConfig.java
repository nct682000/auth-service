package io.github.nct682000.authservice.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth.security")
@Data
public class AuthSecurityConfig {

    /** Failed-login threshold before the account is locked. */
    private int maxLoginAttempts = 5;

    /**
     * How long an account stays locked, and the rolling window over which
     * failed attempts are counted. After this window elapses, the next
     * login attempt auto-unlocks the account.
     */
    private Duration lockoutWindow = Duration.ofMinutes(15);

    /** Validity window for password-reset OTPs. Default: 10 minutes. */
    private Duration otpTtl = Duration.ofMinutes(10);
}

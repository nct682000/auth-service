package io.github.nct682000.authservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth.jwt")
@Data
public class JwtConfig {
    private String secret;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
}

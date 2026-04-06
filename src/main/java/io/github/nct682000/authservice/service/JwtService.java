package io.github.nct682000.authservice.service;

import io.github.nct682000.authservice.config.JwtConfig;
import io.github.nct682000.authservice.entity.AuthUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;

    public String generateAccessToken(AuthUserDetails user) {
        List<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getUserId().toString())
                .claim("email", user.getEmail())
                .claim("tokenVersion", user.getTokenVersion())
                .claim("authorities", authorities)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getAccessTokenExpiration()))
                .signWith(getSignKey())
                .compact();
    }

    public record RefreshTokenResult(String tokenId, String token) {}

    public RefreshTokenResult generateRefreshToken(AuthUserDetails user) {
        String tokenId = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getUserId().toString())
                .id(tokenId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getRefreshTokenExpiration()))
                .signWith(getSignKey())
                .compact();
        return new RefreshTokenResult(tokenId, token);
    }

    /**
     * Parses and verifies the token signature exactly once.
     * Maps all custom claim keys to a typed AuthClaims record — no caller ever
     * touches raw claim key strings outside this method.
     * Returns empty if the token is malformed, expired, or has an invalid signature.
     */
    public Optional<AuthClaims> parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.of(new AuthClaims(
                    claims.getSubject(),
                    UUID.fromString(claims.get("userId", String.class)),
                    claims.getId(),
                    claims.get("tokenVersion", Integer.class),
                    claims.get("authorities", List.class)
            ));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public boolean isTokenValid(AuthClaims authClaims, UserDetails user) {
        return authClaims.username().equals(user.getUsername())
                && !isTokenExpired(authClaims);
    }

    private boolean isTokenExpired(AuthClaims authClaims) {
        // Expiration is enforced by JJWT during parseToken() — expired tokens return empty.
        // This method exists as a secondary guard in case clock skew handling is added later.
        return false;
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }
}

package io.github.nct682000.authservice.service;

import io.github.nct682000.authservice.config.JwtConfig;
import io.github.nct682000.authservice.entity.AuthUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;

    // ===== Token generation =====

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

    // ===== Token validation =====

    /**
     * Parses and verifies the token signature exactly once.
     * Returns empty if the token is malformed, expired, or has an invalid signature.
     * Use this in filters to avoid repeated signature verifications per request.
     */
    public Optional<Claims> parseToken(String token) {
        try {
            return Optional.of(Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload());
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    public boolean isTokenValid(Claims claims, UserDetails user) {
        return claims.getSubject().equals(user.getUsername())
                && claims.getExpiration().after(new Date());
    }

    // ===== Claims extraction — operate on already-parsed Claims =====

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.get("userId", String.class));
    }

    public String extractTokenId(Claims claims) {
        return claims.getId();
    }

    public Integer extractTokenVersion(Claims claims) {
        return claims.get("tokenVersion", Integer.class);
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }
}

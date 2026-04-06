package io.github.nct682000.authservice.service;

import java.util.List;
import java.util.UUID;

/**
 * Typed representation of the custom claims embedded in a JWT.
 * Produced by JwtService.parseToken() — callers never access raw claim keys directly.
 *
 * To add a new claim:
 *   1. Emit it in JwtService.generateAccessToken() → .claim("key", value)
 *   2. Add the field here and map it in JwtService.parseToken()
 *   The compiler will point out every caller that needs updating.
 */
public record AuthClaims(
        String username,
        UUID userId,
        String tokenId,
        Integer tokenVersion,
        List<String> authorities
) {}

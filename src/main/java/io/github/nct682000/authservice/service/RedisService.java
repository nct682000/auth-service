package io.github.nct682000.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token";
    private static final String LOGIN_ATTEMPTS_PREFIX = "login_attempts";
    private static final String OTP_PREFIX = "otp";

    // ===== Refresh token =====

    public void saveRefreshToken(UUID userId, String tokenId, String token, Duration ttl) {
        redisTemplate.opsForValue().set(refreshKey(userId, tokenId), token, ttl);
    }

    public Optional<String> getRefreshToken(UUID userId, String tokenId) {
        Object value = redisTemplate.opsForValue().get(refreshKey(userId, tokenId));
        return Optional.ofNullable(value).map(Object::toString);
    }

    public void deleteRefreshToken(UUID userId, String tokenId) {
        redisTemplate.delete(refreshKey(userId, tokenId));
    }

    public void deleteAllRefreshTokens(UUID userId) {
        Set<String> keys = redisTemplate.keys(REFRESH_TOKEN_PREFIX + ":" + userId + ":*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private String refreshKey(UUID userId, String tokenId) {
        return REFRESH_TOKEN_PREFIX + ":" + userId + ":" + tokenId;
    }

    // ===== Login attempt tracking (brute-force protection) =====

    public long incrementLoginAttempts(String username, Duration ttl) {
        String key = loginAttemptsKey(username);
        Long count = redisTemplate.opsForValue().increment(key);
        long value = Objects.isNull(count) ? 1L : count;
        if (value == 1L) {
            redisTemplate.expire(key, ttl);
        }
        return value;
    }

    public void resetLoginAttempts(String username) {
        redisTemplate.delete(loginAttemptsKey(username));
    }

    private String loginAttemptsKey(String username) {
        return LOGIN_ATTEMPTS_PREFIX + ":" + username;
    }

    // ===== Password reset OTP =====

    public void saveOtp(String email, String hashedOtp, Duration ttl) {
        redisTemplate.opsForValue().set(otpKey(email), hashedOtp, ttl);
    }

    public Optional<String> getOtp(String email) {
        Object value = redisTemplate.opsForValue().get(otpKey(email));
        return Optional.ofNullable(value).map(Object::toString);
    }

    public void deleteOtp(String email) {
        redisTemplate.delete(otpKey(email));
    }

    private String otpKey(String email) {
        return OTP_PREFIX + ":" + email;
    }
}

package io.github.nct682000.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token";

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
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private String refreshKey(UUID userId, String tokenId) {
        return REFRESH_TOKEN_PREFIX + ":" + userId + ":" + tokenId;
    }
}

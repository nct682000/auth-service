package io.github.nct682000.authservice.service;

import io.github.nct682000.authservice.config.AuthSecurityConfig;
import io.github.nct682000.authservice.entity.User;
import io.github.nct682000.authservice.enumeration.UserStatus;
import io.github.nct682000.authservice.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginAttemptService {

    private final RedisService redisService;
    private final UserRepository userRepository;
    private final AuthSecurityConfig securityConfig;

    @Transactional
    public void checkLockout(User user) {
        if (user.getStatus() != UserStatus.LOCKED) {
            return;
        }

        // Admin-locked indefinitely (locked_until == null) — never auto-unlock.
        if (ObjectUtils.isEmpty(user.getLockedUntil())) {
            log.info("Login rejected — account '{}' is admin-locked", user.getUsername());
            throw new LockedException("Account is locked");
        }

        if (user.getLockedUntil().isAfter(LocalDateTime.now())) {
            log.info("Login rejected — account '{}' is locked until {}",
                    user.getUsername(), user.getLockedUntil());
            throw new LockedException("Account is locked");
        }

        // Lockout window has elapsed — auto-unlock and proceed.
        user.setStatus(UserStatus.ACTIVE);
        user.setLockedUntil(null);
        userRepository.save(user);
        log.info("Account '{}' auto-unlocked after lockout window expired", user.getUsername());
    }

    @Transactional
    public void recordFailure(User user) {
        long attempts = redisService.incrementLoginAttempts(
                user.getUsername(), securityConfig.getLockoutWindow());

        log.info("Failed login attempt #{} for user '{}'", attempts, user.getUsername());

        if (attempts >= securityConfig.getMaxLoginAttempts()) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedUntil(LocalDateTime.now().plus(securityConfig.getLockoutWindow()));
            userRepository.save(user);
            redisService.resetLoginAttempts(user.getUsername());
            log.warn("Account '{}' locked until {} after {} failed attempts",
                    user.getUsername(), user.getLockedUntil(), attempts);
        }
    }

    /**
     * Clears the failure counter on successful authentication. A typo
     * followed by a correct password should not count toward future
     * lockouts.
     */
    public void recordSuccess(String username) {
        redisService.resetLoginAttempts(username);
    }
}

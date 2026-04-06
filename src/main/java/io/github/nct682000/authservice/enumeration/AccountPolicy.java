package io.github.nct682000.authservice.enumeration;

import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.util.ObjectUtils;

/**
 * Defines standard account and credential expiry policies.
 *
 * Usage:
 *   AccountPolicy.NEVER.expiresAt()           → null  (no expiry)
 *   AccountPolicy.TRIAL_30_DAYS.expiresAt()   → now + 30 days
 *   AccountPolicy.CREDENTIALS_90_DAYS.expiresAt() → now + 90 days
 */
public enum AccountPolicy {

    /** Account or credentials never expire — default for regular users. */
    NEVER(null),

    /** Account expires after 30 days — for trial or temporary users. */
    TRIAL_30_DAYS(Duration.ofDays(30)),

    /** Credentials expire after 90 days — for compliance password rotation policies. */
    CREDENTIALS_90_DAYS(Duration.ofDays(90));

    private final Duration duration;

    AccountPolicy(Duration duration) {
        this.duration = duration;
    }

    /**
     * Returns the expiry timestamp based on the current time,
     * or null if this policy has no expiry.
     */
    public LocalDateTime expiresAt() {
        return ObjectUtils.isEmpty(duration) ? null : LocalDateTime.now().plus(duration);
    }
}

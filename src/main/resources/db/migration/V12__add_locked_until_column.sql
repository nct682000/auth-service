-- Brute-force protection: time-based account lockout.
--
-- After N consecutive failed login attempts (configurable, default 5)
-- within a rolling window, the user account is locked until `locked_until`.
-- AuthService.login auto-unlocks any account whose `locked_until` has
-- already passed, so users self-recover after the configured window.
--
-- Nullable on purpose:
--   * NULL              → not locked, OR locked indefinitely (admin lock)
--   * timestamp <= now  → lock has expired, login flow will auto-unlock
--   * timestamp >  now  → still locked, login is rejected
--
-- We intentionally do NOT add an index here. `locked_until` is read only
-- on the login path AFTER username lookup (which already uses the unique
-- index on username), so it is fetched as part of the same row read.
-- Adding an index would slow writes with no read benefit.

ALTER TABLE "user"
    ADD COLUMN locked_until TIMESTAMP NULL;

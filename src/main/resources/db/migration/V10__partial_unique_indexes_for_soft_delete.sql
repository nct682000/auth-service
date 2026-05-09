-- Replace column-level UNIQUE constraints with partial UNIQUE indexes scoped
-- to active (non-deleted) rows. This aligns DB-level uniqueness with the
-- soft-delete pattern enforced by @SQLRestriction("deleted_at IS NULL"),
-- so a soft-deleted row no longer blocks recreating a record with the same
-- username / name / email.

-- Drop column-level UNIQUE constraints (auto-named *_key by Postgres).
ALTER TABLE "user"     DROP CONSTRAINT user_username_key;
ALTER TABLE role       DROP CONSTRAINT role_name_key;
ALTER TABLE permission DROP CONSTRAINT permission_name_key;

-- Drop V6's now-redundant non-unique partial indexes.
-- The new partial UNIQUE indexes below serve both jobs: enforce uniqueness
-- on live rows AND help the query planner with @SQLRestriction-filtered lookups.
DROP INDEX IF EXISTS idx_user_username_active;
DROP INDEX IF EXISTS idx_role_name_active;
DROP INDEX IF EXISTS idx_permission_name_active;

-- Partial UNIQUE indexes — uniqueness enforced only on rows where deleted_at IS NULL.
CREATE UNIQUE INDEX uk_user_username_active   ON "user"(username) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_role_name_active       ON role(name)       WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_permission_name_active ON permission(name) WHERE deleted_at IS NULL;

-- Email had no DB-level uniqueness before this migration.
-- The AND email IS NOT NULL clause is documentation: we deliberately don't
-- constrain users with no email. (Postgres treats NULLs as distinct in
-- unique indexes by default, so this is also functionally redundant — but
-- the explicit predicate makes intent crystal clear to future readers.)
CREATE UNIQUE INDEX uk_user_email_active      ON "user"(email)    WHERE deleted_at IS NULL AND email IS NOT NULL;

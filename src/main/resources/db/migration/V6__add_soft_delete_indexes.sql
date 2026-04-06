-- Replace the plain username index with a partial index scoped to active (non-deleted) rows.
-- All queries now include deleted_at IS NULL (via @SQLRestriction), so the old index is no longer hit.
DROP INDEX IF EXISTS idx_user_username;

-- Partial indexes: only index rows where deleted_at IS NULL.
-- This keeps the index small and ensures query planner uses it with the soft-delete filter.
CREATE INDEX idx_user_username_active    ON "user"(username)  WHERE deleted_at IS NULL;
CREATE INDEX idx_role_name_active        ON role(name)         WHERE deleted_at IS NULL;
CREATE INDEX idx_permission_name_active  ON permission(name)   WHERE deleted_at IS NULL;

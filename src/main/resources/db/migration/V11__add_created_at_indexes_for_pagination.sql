-- Pagination support indexes.
--
-- All admin list endpoints (GET /admin/users, /roles, /permissions) order by
-- created_at DESC with id DESC as a deterministic tie-breaker. Without these
-- indexes, Postgres performs a full sequential scan + sort for every page
-- request -- O(N log N) on a table that may grow into the millions of rows
-- (the user table in particular).
--
-- The indexes are PARTIAL on `deleted_at IS NULL` so:
--   1. They stay small (soft-deleted rows are excluded).
--   2. The planner uses them when @SQLRestriction adds the deleted_at filter.
--
-- The composite (created_at DESC, id DESC) matches the query's ORDER BY
-- exactly, so Postgres can stream rows directly from the index without a
-- separate sort step. id DESC is included because tie-breaking on id keeps
-- offset pagination stable when multiple rows share a created_at (common
-- after seed migrations or bulk imports).
--
-- Note: when offset depth becomes a real bottleneck (typically OFFSET > ~10k),
-- the next step is keyset/cursor pagination. The index above is the
-- prerequisite for both offset AND keyset, so this migration is forward-
-- compatible.

CREATE INDEX idx_user_created_at_active
    ON "user"(created_at DESC, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_role_created_at_active
    ON role(created_at DESC, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_permission_created_at_active
    ON permission(created_at DESC, id DESC)
    WHERE deleted_at IS NULL;

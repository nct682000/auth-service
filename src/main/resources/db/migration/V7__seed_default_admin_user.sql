-- Default admin user (password: "admin", BCrypt cost 12)
-- IMPORTANT: Change this password before deploying to any non-local environment.
INSERT INTO "user" (id, username, email, password, status, token_version, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@localhost',
    '$2a$12$K8AawP846ujsU2DRpbgbnudICaCyhn91JHZQL7WjHTIX9v//N1LUG',
    'ACTIVE',
    1,
    NOW(),
    NOW()
);

-- Assign admin role to the default admin user
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM "user" u, role r
WHERE u.username = 'admin'
  AND r.name = 'admin';

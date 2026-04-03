-- enable uuid
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- roles
INSERT INTO role (id, name, created_at, updated_at)
VALUES (gen_random_uuid(), 'user', NOW(), NOW()),
       (gen_random_uuid(), 'admin', NOW(), NOW());

-- permissions
INSERT INTO permission (id, name, created_at, updated_at)
VALUES (gen_random_uuid(), 'view_my_profile', NOW(), NOW()),
       (gen_random_uuid(), 'edit_my_profile', NOW(), NOW()),
       (gen_random_uuid(), 'remove_my_profile', NOW(), NOW()),
       (gen_random_uuid(), 'view_all_profiles', NOW(), NOW()),
       (gen_random_uuid(), 'edit_all_profiles', NOW(), NOW());

-- user role → basic permissions
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'user'
  AND p.name IN ('view_my_profile', 'edit_my_profile', 'remove_my_profile');

-- admin role → full permissions
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'admin';
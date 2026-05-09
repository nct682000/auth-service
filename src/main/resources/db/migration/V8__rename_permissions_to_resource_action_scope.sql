-- Rename permissions to resource:action:scope naming convention
UPDATE permission SET name = 'profile:read:own',   updated_at = NOW() WHERE name = 'view_my_profile';
UPDATE permission SET name = 'profile:write:own',  updated_at = NOW() WHERE name = 'edit_my_profile';
UPDATE permission SET name = 'profile:delete:own', updated_at = NOW() WHERE name = 'remove_my_profile';
UPDATE permission SET name = 'profile:read:all',   updated_at = NOW() WHERE name = 'view_all_profiles';
UPDATE permission SET name = 'profile:write:all',  updated_at = NOW() WHERE name = 'edit_all_profiles';

-- Add role and permission management permissions
INSERT INTO permission (id, name, created_at, updated_at)
VALUES (gen_random_uuid(), 'role:manage',       NOW(), NOW()),
       (gen_random_uuid(), 'permission:manage', NOW(), NOW());

-- Assign new permissions to admin role
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'admin'
  AND p.name IN ('role:manage', 'permission:manage');

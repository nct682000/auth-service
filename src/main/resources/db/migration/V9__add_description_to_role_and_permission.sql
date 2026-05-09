-- Add description column to role and permission tables.
-- Nullable: descriptions are explanatory metadata, not load-bearing.
-- VARCHAR(255): long enough to be useful, short enough to discourage essays.

ALTER TABLE role       ADD COLUMN description VARCHAR(255);
ALTER TABLE permission ADD COLUMN description VARCHAR(255);

-- Backfill existing seeded roles
UPDATE role SET description = 'Full administrative access — manage users, roles, and permissions',
                updated_at  = NOW()
 WHERE name = 'admin';

UPDATE role SET description = 'Standard end user — access to own profile only',
                updated_at  = NOW()
 WHERE name = 'user';

-- Backfill existing seeded permissions
UPDATE permission SET description = 'Read own profile',
                      updated_at  = NOW()
 WHERE name = 'profile:read:own';

UPDATE permission SET description = 'Edit own profile',
                      updated_at  = NOW()
 WHERE name = 'profile:write:own';

UPDATE permission SET description = 'Delete own account',
                      updated_at  = NOW()
 WHERE name = 'profile:delete:own';

UPDATE permission SET description = 'Read any user''s profile',
                      updated_at  = NOW()
 WHERE name = 'profile:read:all';

UPDATE permission SET description = 'Edit any user''s profile or status',
                      updated_at  = NOW()
 WHERE name = 'profile:write:all';

UPDATE permission SET description = 'Create, delete, and assign roles',
                      updated_at  = NOW()
 WHERE name = 'role:manage';

UPDATE permission SET description = 'Create, delete, and assign permissions',
                      updated_at  = NOW()
 WHERE name = 'permission:manage';

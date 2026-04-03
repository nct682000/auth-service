ALTER TABLE user_role
    ADD CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE;

ALTER TABLE user_role
    ADD CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE;

ALTER TABLE role_permission
    ADD CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE;

ALTER TABLE role_permission
    ADD CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE;
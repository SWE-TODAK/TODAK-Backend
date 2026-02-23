CREATE UNIQUE INDEX IF NOT EXISTS ux_users_nickname
    ON users(nickname);

ALTER TABLE user_identities
    ADD CONSTRAINT uq_user_provider UNIQUE (user_id, provider);
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

DROP INDEX IF EXISTS ux_users_email;
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email
    ON users(email)
    WHERE email IS NOT NULL AND deleted_at IS NULL;

DROP INDEX IF EXISTS ux_users_nickname;
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_nickname
    ON users(nickname)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS ix_users_deleted_at
    ON users(deleted_at);

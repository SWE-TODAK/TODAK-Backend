CREATE TABLE IF NOT EXISTS user_auth (
                                         user_id UUID PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    password_hash       TEXT NOT NULL,
    password_updated_at TIMESTAMPTZ,
    last_login_at       TIMESTAMPTZ
    );

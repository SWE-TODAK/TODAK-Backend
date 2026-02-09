-- V0__init.sql (PostgreSQL)
-- Users(프로필/기본정보) + User Identities(로그인 식별자) + Refresh Tokens(토큰)

-- 0) UUID 생성 함수
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1) Enum: gender
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'gender_enum') THEN
CREATE TYPE gender_enum AS ENUM ('MALE', 'FEMALE');
END IF;
END $$;

-- 2) users: 프로필/기본정보
CREATE TABLE IF NOT EXISTS users (
                                     user_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email              VARCHAR(255),            -- UNIQUE + NULL 허용 (카카오만 쓰면 카카오에서 받아오거나 없을 수도)
    name               VARCHAR(50),             -- 본명
    birth_date         DATE,                    -- 생년월일
    gender             gender_enum,             -- 'MALE' | 'FEMALE'
    profile_image_url  TEXT,                    -- 프로필 이미지

    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
    );

-- email은 값이 있을 때만 유니크 (NULL 여러 개 허용)
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email
    ON users(email)
    WHERE email IS NOT NULL;


-- 3) user_identities: 로그인 식별자(확장 고려해서 분리)
-- - 예: LOCAL(username/email), KAKAO(kakao_id)
CREATE TABLE IF NOT EXISTS user_identities (
                                               id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    provider          VARCHAR(30) NOT NULL,       -- 'LOCAL', 'KAKAO' 등
    provider_user_id  VARCHAR(255) NOT NULL,      -- kakao_id / local_id(예: email) 등
    provider_email    VARCHAR(255),               -- provider에서 내려주는 email(옵션)

    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_user_identity UNIQUE (provider, provider_user_id)
    );

CREATE INDEX IF NOT EXISTS ix_user_identities_user_id
    ON user_identities(user_id);


-- 4) refresh_tokens: 토큰(리프레시) 관리
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    token         TEXT NOT NULL,
    expires_at    TIMESTAMPTZ NOT NULL,

    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at    TIMESTAMPTZ,

    CONSTRAINT uq_refresh_token UNIQUE (token)
    );

CREATE INDEX IF NOT EXISTS ix_refresh_tokens_user_id
    ON refresh_tokens(user_id);

CREATE INDEX IF NOT EXISTS ix_refresh_tokens_expires_at
    ON refresh_tokens(expires_at);


-- 5) updated_at 자동 갱신 트리거 (선택인데 V0에 넣어두면 편함)
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_users_set_updated_at') THEN
CREATE TRIGGER trg_users_set_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
END IF;
END $$;

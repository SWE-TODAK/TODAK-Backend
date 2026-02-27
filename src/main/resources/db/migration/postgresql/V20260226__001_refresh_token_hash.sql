-- refresh_tokens: token(text) -> token_hash(varchar(64))로 전환
-- 전제: 기존 refresh 토큰은 무효화(TRUNCATE)하고 진행

-- 1) 기존 토큰 데이터 제거
TRUNCATE TABLE refresh_tokens;

-- 2) 기존 token 유니크 제약 제거
ALTER TABLE refresh_tokens
DROP CONSTRAINT IF EXISTS uq_refresh_token;

-- (환경에 따라 unique constraint가 만든 index가 같이 지워지기도 해서 안전하게 한 번 더)
DROP INDEX IF EXISTS uq_refresh_token;

-- 3) token_hash 컬럼 추가 (SHA-256 hex = 64 chars)
ALTER TABLE refresh_tokens
    ADD COLUMN token_hash VARCHAR(64);

-- 4) NOT NULL 적용
ALTER TABLE refresh_tokens
    ALTER COLUMN token_hash SET NOT NULL;

-- 5) token_hash 유니크 제약 추가
ALTER TABLE refresh_tokens
    ADD CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash);

-- 6) 기존 token 컬럼 제거
ALTER TABLE refresh_tokens
DROP COLUMN token;
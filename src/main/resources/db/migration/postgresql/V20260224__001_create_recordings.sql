-- V4__create_recordings.sql

CREATE TABLE IF NOT EXISTS recordings (
                                          recording_id UUID PRIMARY KEY,
                                          user_id UUID NOT NULL,

                                          status VARCHAR(30) NOT NULL,

                                          storage_key TEXT,
                                          mime_type VARCHAR(100),
                                          duration_ms INTEGER,
                                          sample_rate INTEGER,

                                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                          CONSTRAINT fk_recordings_user
                                              FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_recordings_user_id ON recordings(user_id);
CREATE INDEX IF NOT EXISTS idx_recordings_status ON recordings(status);
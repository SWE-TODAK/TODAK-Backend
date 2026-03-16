-- V5__create_summaries.sql

CREATE TABLE IF NOT EXISTS summaries (
                                         summary_id UUID PRIMARY KEY,
                                         recording_id UUID NOT NULL,

                                         content TEXT NOT NULL,
                                         intro TEXT NOT NULL,

                                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                         CONSTRAINT fk_summaries_recording
                                             FOREIGN KEY (recording_id) REFERENCES recordings(recording_id)
                                                 ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_summaries_recording_id ON summaries(recording_id);

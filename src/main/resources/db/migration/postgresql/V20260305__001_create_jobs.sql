CREATE TABLE jobs (
                      job_id UUID PRIMARY KEY,

                      recording_id UUID NOT NULL,

                      job_type VARCHAR(30) NOT NULL,
                      status VARCHAR(30) NOT NULL,

                      attempt_count INTEGER NOT NULL DEFAULT 0,
                      error_message TEXT,

                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                      CONSTRAINT fk_jobs_recording
                          FOREIGN KEY (recording_id)
                              REFERENCES recordings(recording_id)
                              ON DELETE CASCADE
);

CREATE INDEX idx_jobs_recording_id ON jobs(recording_id);
CREATE INDEX idx_jobs_status ON jobs(status);

CREATE UNIQUE INDEX ux_jobs_recording_type ON jobs(recording_id, job_type);
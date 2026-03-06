CREATE TABLE IF NOT EXISTS health_metrics (
                                              metric_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    unit        VARCHAR(20) NOT NULL,
    is_custom   BOOLEAN DEFAULT FALSE,
    metric_type VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS health_metric_values (
                                                    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_id    UUID NOT NULL REFERENCES health_metrics(metric_id) ON DELETE CASCADE,
    value        DOUBLE PRECISION,
    systolic     INTEGER,
    diastolic    INTEGER,
    before_meal  INTEGER,
    after_meal   INTEGER,
    total_chol   DOUBLE PRECISION,
    triglyceride DOUBLE PRECISION,
    hdl          DOUBLE PRECISION,
    ldl          DOUBLE PRECISION,
    recorded_at  TIMESTAMPTZ NOT NULL DEFAULT now()
    );
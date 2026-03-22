-- 1. health_metrics 테이블 생성
CREATE TABLE health_metrics (
                                metric_id UUID PRIMARY KEY,
                                user_id UUID NOT NULL,
                                name VARCHAR(255) NOT NULL,
                                unit VARCHAR(255) NOT NULL,
                                is_custom BOOLEAN NOT NULL,
                                metric_type VARCHAR(255),
                                CONSTRAINT fk_health_metric_user FOREIGN KEY (user_id) REFERENCES users (id) -- users 테이블의 id 컬럼 참조
);

-- 2. health_metric_values 테이블 생성
CREATE TABLE health_metric_values (
                                      id UUID PRIMARY KEY,
                                      metric_id UUID NOT NULL,
                                      value DOUBLE PRECISION,
                                      systolic INTEGER,
                                      diastolic INTEGER,
                                      before_meal INTEGER,
                                      after_meal INTEGER,
                                      total_chol DOUBLE PRECISION,
                                      triglyceride DOUBLE PRECISION,
                                      hdl DOUBLE PRECISION,
                                      ldl DOUBLE PRECISION,
                                      recorded_at TIMESTAMP NOT NULL,
                                      CONSTRAINT fk_health_metric_value_metric FOREIGN KEY (metric_id) REFERENCES health_metrics (metric_id)
);
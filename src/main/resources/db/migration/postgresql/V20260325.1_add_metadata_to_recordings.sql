ALTER TABLE recordings
    ADD COLUMN hospital_name VARCHAR(255),
    ADD COLUMN disease_name VARCHAR(255),
    ADD COLUMN doctor_name VARCHAR(100),
    ADD COLUMN department_name VARCHAR(100),
    ADD COLUMN consulted_at TIMESTAMP WITH TIME ZONE;
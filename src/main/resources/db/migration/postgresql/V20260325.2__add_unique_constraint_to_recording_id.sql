ALTER TABLE summaries
    ADD CONSTRAINT ux_summaries_recording_id UNIQUE (recording_id);
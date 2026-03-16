create table transcriptions (
                                transcription_id uuid primary key,
                                recording_id uuid not null unique,
                                transcript_text text not null,
                                language varchar(20),
                                provider varchar(50),
                                model varchar(100),
                                meta_json text,
                                created_at timestamptz not null,
                                updated_at timestamptz not null
);

alter table transcriptions
    add constraint fk_transcriptions_recording
        foreign key (recording_id) references recordings(recording_id);
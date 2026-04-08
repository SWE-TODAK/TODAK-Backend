create table if not exists password_change_verifications (
    user_id uuid primary key,
    email varchar(255) not null,
    code_hash text not null,
    expires_at timestamptz not null,
    sent_at timestamptz not null,
    used_at timestamptz null,
    failed_attempts integer not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

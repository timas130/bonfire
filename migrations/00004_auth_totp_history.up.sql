create table totp_attempts (
    user_id bigint not null references users on delete cascade,
    created_at timestamptz not null default now()
);

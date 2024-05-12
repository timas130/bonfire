create table level_cache (
    user_id bigint not null references users on delete cascade primary key,
    level bigint not null,
    recount_report jsonb not null,
    updated_at timestamptz not null default now()
);

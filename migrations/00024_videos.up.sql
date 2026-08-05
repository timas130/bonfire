create table videos (
    id bigint primary key generated always as identity,
    video_id text not null unique,
    user_id bigint not null references users on delete set default default get_deleted_user_id(),
    publication_id bigint,
    status int not null default 1,
    error text,
    duration_ms bigint,
    width int,
    height int,
    size_bytes bigint,
    audio_gain real,
    encoding_version int not null default 1,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index videos_user_created_idx on videos (user_id, created_at desc);
create index videos_status_idx on videos (status) where status in (1, 2, 3, 6);
create index videos_publication_idx on videos (publication_id) where publication_id is not null;

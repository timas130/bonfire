create table notifications (
    id bigint not null generated always as identity primary key,
    user_id bigint not null references users on delete cascade,
    notification_type int not null,
    payload jsonb not null,
    created_at timestamptz not null default now(),
    read bool not null default false
);

create table notification_tokens (
    session_id bigint not null references sessions on delete cascade primary key,
    service int not null,
    token text not null
);

create table notification_profiles (
    user_id bigint not null references users on delete cascade primary key,
    -- do not disturb end time
    dnd_end_time timestamptz null default null
);

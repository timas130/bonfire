create table sessions (
    id bigint not null generated always as identity primary key,
    user_id bigint not null references users on delete cascade,
    account_id bigint null references auth_sources on delete set null,
    provider int null,
    ip inet null,
    created_at timestamptz not null default now(),
    last_refreshed timestamptz not null default now(),
    expires timestamptz null,
    user_agent text null,
    refresh_token varchar(32) not null unique
);

create table oauth_flows (
    ua_token varchar(32) not null unique,
    csrf_token varchar(32) not null unique,
    nonce varchar(32) not null,
    created_at timestamptz not null default now(),
    expires timestamptz not null
);

create table tfa_flows (
    user_id bigint not null references users on delete cascade,
    id varchar(32) not null unique,
    expires timestamptz not null,
    created_at timestamptz not null default now(),
    ip inet null,
    user_agent text null,
    action_type int not null,
    action_data text not null default '',
    completed bool not null default false
);

create table recovery_flows (
    user_id bigint not null references users on delete cascade,
    id varchar(32) not null unique,
    expires timestamptz not null,
    created_at timestamptz not null default now(),
    ip inet null,
    user_agent text null
);

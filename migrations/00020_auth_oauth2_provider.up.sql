create table oauth2_clients (
    id bigint not null generated always as identity primary key,
    client_id text not null unique,
    client_secret text not null,
    display_name text not null,
    privacy_policy_url text not null,
    tos_url text null,
    official bool not null default false,
    allowed_scopes text[] not null default '{"openid", "profile", "email"}',
    enforce_code_challenge bool not null default false,
    created_at timestamptz not null default now()
);

create index on oauth2_clients (client_id);

create table oauth2_redirect_uris (
    id bigint not null generated always as identity primary key,
    client_id bigint not null references oauth2_clients on delete cascade,
    exact_uri text not null
);

create table oauth2_grants (
    id bigint not null generated always as identity primary key,
    client_id bigint not null references oauth2_clients on delete cascade,
    user_id bigint not null references users on delete cascade,
    scope text[] not null,
    created_at timestamptz not null default now(),
    last_used_at timestamptz not null default now()
);

create unique index on oauth2_grants (user_id, client_id);

create table oauth2_flows_as (
    id bigint not null generated always as identity primary key,
    session_id bigint not null references sessions on delete cascade,
    client_id bigint not null references oauth2_clients on delete cascade,
    grant_id bigint null references oauth2_grants on delete cascade,
    redirect_uri text not null,
    raw_redirect_uri text null,
    scopes text[] not null,
    state text null,
    nonce text null,
    code_challenge text null,
    code_challenge_method text null,
    code text null,
    access_token text null,
    refresh_token text null,
    created_at timestamptz not null default now(),
    authorized_at timestamptz null,
    token_requested_at timestamptz null,
    access_token_expires_at timestamptz null,
    refresh_token_expires_at timestamptz null
);

create or replace function merge_arrays(a1 anyarray, a2 anyarray) returns anyarray as
$$
    select array_agg(x order by x) from (select distinct unnest($1 || $2) as x) s;
$$ language sql strict;

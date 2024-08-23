create table gif_shares (
    user_id bigint not null references users on delete cascade,
    gif_id text not null,
    search_query text null,
    country text not null,
    locale text null,
    created_at timestamptz not null default now()
);

create table gif_favourites (
    user_id bigint not null references users on delete cascade,
    gif_id text not null,
    created_at timestamptz not null default now(),
    last_used_at timestamptz not null default now()
);

create unique index on gif_favourites (user_id, gif_id);
create index on gif_shares (user_id);

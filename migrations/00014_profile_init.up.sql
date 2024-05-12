create table badges (
    id bigint not null generated always as identity primary key,
    user_id bigint not null references users on delete cascade,
    name text not null,
    description text not null,
    mini_image_id bigint not null,
    image_id bigint not null,
    fandom_id bigint null,
    link text null,
    created_at timestamptz not null default now()
);

create table account_customization (
    user_id bigint not null references users on delete cascade primary key,
    nickname_color int null,
    active_badge bigint null references badges on delete set null
);

create table profile_customization (
    user_id bigint not null references users on delete cascade primary key,
    show_badge_shelf boolean not null default true
);

create table badge_shelf_items (
    user_id bigint not null references users on delete cascade,
    badge_id bigint not null references badges on delete cascade,
    "order" int not null
);

create unique index on badge_shelf_items (user_id, "order");

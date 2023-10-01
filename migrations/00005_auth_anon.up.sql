create table anon_users (
    id bigint not null generated always as identity primary key,
    register_ip inet not null,
    register_ua text not null,
    clearance_score int null
);

alter table users drop constraint users_email_key;
alter table users alter column email drop not null;
alter table users add column anon_id bigint null references anon_users on delete cascade;

create unique index users_anon_id_email_key on users (anon_id, email);

create table clearance_tokens (
    id bigint not null generated always as identity primary key,
    ip_address inet not null,
    user_agent text not null,
    finished bool not null default false,
    consumed bool not null default false,
    score int null,
    created_at timestamptz not null default now(),
    expires_at timestamptz not null
);

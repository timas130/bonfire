create table additional_levels (
    user_id bigint not null references users on delete cascade primary key,
    amount int not null,
    reason text not null,
    created_at timestamptz not null default now()
);

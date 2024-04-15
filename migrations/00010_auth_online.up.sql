alter table sessions add column last_online timestamptz not null default now();

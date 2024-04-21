alter table notification_tokens add column created_at timestamptz not null default now();

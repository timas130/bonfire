create table temp_images (
    key text not null primary key,
    user_id bigint not null references users on delete set default default get_deleted_user_id(),
    upload_type text not null,
    size bigint not null,
    gif bool not null,
    created_at timestamptz not null default now()
);

create table user_integrity_checks (
    id bigint not null generated always as identity primary key,
    user_id bigint not null references users on delete cascade,
    status text not null,
    package_name text,
    app_license text,
    app_recognition text,
    cert_digest text,
    device_integrity boolean,
    basic_integrity boolean,
    strong_integrity boolean,
    device_activity text,
    created_at timestamptz not null default now()
);

create table security_intentions (
    id bigint not null generated always as identity primary key,
    token text not null unique,
    user_id bigint not null references users on delete cascade,
    intention_type int not null,
    attempts int not null default 0,
    created_at timestamptz not null default now(),
    passed_at timestamptz null default null,
    check_id bigint null references user_integrity_checks on delete cascade
);


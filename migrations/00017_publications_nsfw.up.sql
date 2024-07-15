alter table units add column nsfw boolean not null default false;

create table real_user_birthdays (
    user_id bigint not null references users on delete cascade primary key,
    birthday date not null
);

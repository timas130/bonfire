-- noinspection SqlWithoutWhere
update users
set created_at = coalesce(
    to_timestamp((select date_create from accounts where accounts.id = users.id) / 1000),
    users.created_at
);

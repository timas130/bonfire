update fandoms
set subscribers_count = (
    select count(*)
    from collisions
    where collision_type = 20
    and value_1 != 1
    and collision_id = fandoms.id
);

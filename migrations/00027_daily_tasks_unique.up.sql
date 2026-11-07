do $$
begin
    if exists (
        select 1 from pg_constraint
        where conrelid = 'daily_tasks'::regclass
          and conname = 'daily_tasks_account_id_key'
          and pg_get_constraintdef(oid) = 'UNIQUE (account_id)'
    ) then
        alter table daily_tasks
            drop constraint daily_tasks_account_id_key,
            add constraint daily_tasks_account_id_date_key unique (account_id, date);
    end if;
end
$$;

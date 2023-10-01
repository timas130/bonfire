drop index users_anon_id_email_key;
alter table users drop column anon_id;
alter table users alter column email set not null;
alter table users add constraint users_email_key unique (email);

drop table anon_users;
drop table clearance_tokens;

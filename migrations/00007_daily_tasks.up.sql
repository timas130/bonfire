create table daily_tasks (
	account_id bigint not null unique,
	date date not null,
	json_db json not null,
	progress double precision default 0 not null,
	level_multiplier double precision default 0 not null,
	combo_multiplier double precision default 0 not null,
	checked_in boolean default false not null
);

create table random_seeds (
	date date not null unique,
	seed bytea not null
);

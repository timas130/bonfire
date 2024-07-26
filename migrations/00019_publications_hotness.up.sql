create or replace function test_hotness(score bigint, date_create bigint) returns double precision as
$$
declare ord double precision;
        sign double precision;
        seconds double precision;
begin
    select log10(greatest(abs(score / 100), 1)::double precision) into ord;
    if score > 0 then
        select 1 into sign;
    else
        select -1 into sign;
    end if;
    select (date_create / 1000) - 1672520400 into seconds;
    return sign * ord + seconds / 45000;
end;
$$ language plpgsql immutable;

alter table units add column if not exists
    hotness double precision
        generated always as ( test_hotness(karma_count, date_create)::double precision )
        stored;

create trigger on_update_account
    before update on auth_sources
    for each row execute procedure update_modified();

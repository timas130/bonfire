package com.dzen.campfire.server.tables

object TShadowBans {
    val NAME = "shadow_bans"
    val account_id = "account_id"
    val reason = "reason"
    val created_at = "created_at"

    /* create table shadow_bans (
         account_id bigint not null primary key,
         reason text not null,
         created_at timestamptz not null default now()
       ); */
}

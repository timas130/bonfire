package com.dzen.campfire.server.tables

import com.sup.dev.java_pc.sql.Sql

object TWikiPages {

    val NAME = "wiki_pages"

    val id = "id"
    val item_id = "item_id"
    val item_data = "item_data"
    val date_create = "date_create"
    val creator_id = "creator_id"
    val language_id = "language_id"
    val event_type = "event_type"
    val wiki_status = "wiki_status"

    val ITEM_STATUS = Sql.IFNULL("(SELECT ${TWikiItems.status} FROM ${TWikiItems.NAME} u WHERE u.${TWikiItems.id}=$NAME.$item_id)", 0)
    val CREATOR_NAME = Sql.IFNULL("(SELECT ${TAccounts.name} FROM ${TAccounts.NAME} u WHERE u.${TAccounts.id}=$NAME.$creator_id)", "''")
    val CREATOR_IMAGE_ID = Sql.IFNULL("(SELECT ${TAccounts.img_id} FROM ${TAccounts.NAME} u WHERE u.${TAccounts.id}=$NAME.$creator_id)", 0)

}

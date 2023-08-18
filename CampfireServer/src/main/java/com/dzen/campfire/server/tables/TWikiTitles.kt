package com.dzen.campfire.server.tables

import com.sup.dev.java_pc.sql.Sql

object TWikiTitles {

    val NAME = "wiki_titles"

    val id = "id"
    val item_id = "item_id"
    val parent_item_id = "parent_item_id"
    val fandom_id = "fandom_id"
    val item_data = "item_data"
    val date_create = "date_create"
    val type = "type"
    val creator_id = "creator_id"
    val wiki_status = "wiki_status"
    val priority = "priority"

    val ITEM_STATUS = Sql.IFNULL("(SELECT ${TWikiItems.status} FROM ${TWikiItems.NAME} u WHERE u.${TWikiItems.id}=${NAME}.${item_id})", 0)


}

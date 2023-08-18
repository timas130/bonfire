package com.dzen.campfire.server.tables

import com.dzen.campfire.api.API
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect

object TFandoms {


    val NAME = "fandoms"

    val id = "id"
    val name = "name"
    val creator_id = "creator_id"
    val image_id = "image_id"
    val image_title_id = "image_title_id"
    val date_create = "date_create"
    val status = "status"
    val subscribers_count = "subscribers_count"
    val fandom_category = "fandom_category"
    val fandom_closed = "fandom_closed"
    val karma_cof = "karma_cof"

    fun other_names(languageId: Long) = Sql.IFNULL(SqlQuerySelect(TCollisions.NAME, TCollisions.value_2)
            .where(TCollisions.owner_id, "=", "$NAME.$id")
            .where(TCollisions.collision_id, "=", languageId)
            .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_NAMES), "'~~~'")

}

package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect

object ControllerViceroy {
    fun getViceroyId(fandomId: Long, languageId: Long): Long {
        return Database.select(
            "EFandomsViceroyGet", SqlQuerySelect(TCollisions.NAME, TCollisions.value_1)
                .where(TCollisions.owner_id, "=", fandomId)
                .where(TCollisions.collision_id, "=", languageId)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_VICEROY)
        ).nextLongOrZero()
    }

    fun getViceroyCount(accountId: Long): Long {
        return Database.select(
            "ControllerViceroy.getViceroyCount",
            SqlQuerySelect(TCollisions.NAME, Sql.COUNT_DISTINCT(TCollisions.owner_id))
                .where(TCollisions.value_1, "=", accountId)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_VICEROY)
        ).nextLongOrZero()
    }
}

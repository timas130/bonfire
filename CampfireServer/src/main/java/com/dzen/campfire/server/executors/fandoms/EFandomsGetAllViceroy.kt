package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAllViceroy
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TFandoms
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EFandomsGetAllViceroy : RFandomsGetAllViceroy(0, 0) {
    override fun execute(): Response {
        val select = SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
                .join(ControllerFandom.instanceSelect())
                .where(TFandoms.NAME + "." + TFandoms.id, "=", TCollisions.owner_id)
                .where(TCollisions.value_1, "=", accountId)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_VICEROY)
                .where(TFandoms.NAME + "." + TFandoms.status, "=", API.STATUS_PUBLIC)
                .sort(TFandoms.subscribers_count, false)
                .offset_count(offset, COUNT)

        val v = Database.select("EFandomsGetAllViceroy",select)
        val fandoms = Array(v.rowsCount) {
            val languageId: Long = v.next()
            val fandom = ControllerFandom.parseSelectOne(v)
            fandom.languageId = languageId
            fandom
        }

        return Response(fandoms)
    }
}

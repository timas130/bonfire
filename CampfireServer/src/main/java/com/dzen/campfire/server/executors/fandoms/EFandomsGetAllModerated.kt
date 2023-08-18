package com.dzen.campfire.server.executors.fandoms


import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAllModerated
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EFandomsGetAllModerated : RFandomsGetAllModerated(0, 0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {
        val select = SqlQuerySelect(TCollisions.NAME, TCollisions.collision_sub_id)
                .join(ControllerFandom.instanceSelect())
                .where(TFandoms.NAME + "." + TFandoms.id, "=", TCollisions.collision_id)
                .where(TCollisions.owner_id, "=", accountId)
                .where(TCollisions.value_1, ">=", API.LVL_MODERATOR_BLOCK.karmaCount)
                .where(TCollisions.collision_type, "=", API.COLLISION_KARMA_30)
                .where(TFandoms.NAME + "." + TFandoms.status, "=", API.STATUS_PUBLIC)
                .sort(TFandoms.subscribers_count, false)
                .offset_count(offset, COUNT)
        
        val v = Database.select("EFandomsGetAllModerated",select)
        val fandoms = Array(v.rowsCount) {
            val languageId: Long = v.next()
            val fandom = ControllerFandom.parseSelectOne(v)
            fandom.languageId = languageId
            fandom
        }

        return Response(fandoms)
    }

}

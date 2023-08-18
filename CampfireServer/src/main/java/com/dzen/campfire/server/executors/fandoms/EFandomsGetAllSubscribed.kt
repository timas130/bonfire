package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAllSubscribed
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TFandoms
import com.sup.dev.java.classes.items.Item2
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere
import java.util.*

class EFandomsGetAllSubscribed : RFandomsGetAllSubscribed(0, 0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {
        val vv = Database.select("EFandomsGetAllSubscribed.execute 1", SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id, TCollisions.collision_sub_id)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_SUBSCRIBE)
                .where(TCollisions.owner_id, "=", accountId)
                .sort(TCollisions.collision_date_create, false))

        if (vv.isEmpty) return Response(emptyArray())

        val infoArray = Array(vv.rowsCount) { Item2(vv.next<Long>(), vv.next<Long>()) }
        val ids = Array(infoArray.size) { infoArray[it].a1 }

        val select = ControllerFandom.instanceSelect()
                .where(TFandoms.status, "=", API.STATUS_PUBLIC)
                .where(SqlWhere.WhereIN(TFandoms.id, ids))
                .sort(TFandoms.subscribers_count, false, TFandoms.date_create)
                .offset_count(offset, COUNT)


        val v = Database.select("EFandomsGetAllSubscribed.execute 2", select)

        val fandomsResult = ArrayList<Fandom>()

        for (i in 0 until v.rowsCount) {
            val fandom = ControllerFandom.parseSelectOne(v)

            for (anInfoArray in infoArray)
                if (anInfoArray.a1 == fandom.id) {
                    val f = Fandom()
                    f.id = fandom.id
                    f.name = fandom.name
                    f.imageId = fandom.imageId
                    f.imageTitleId = fandom.imageTitleId
                    f.dateCreate = fandom.dateCreate
                    f.status = fandom.status
                    f.subscribesCount = fandom.subscribesCount
                    f.languageId = anInfoArray.a2
                    fandomsResult.add(f)
                }
        }

        return Response(Array(fandomsResult.size) { fandomsResult[it] })
    }

}

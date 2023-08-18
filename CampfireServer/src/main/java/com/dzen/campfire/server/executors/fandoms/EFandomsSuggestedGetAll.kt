package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.fandoms.RFandomsSuggestedGetAll
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EFandomsSuggestedGetAll : RFandomsSuggestedGetAll(0) {

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOMS_ACCEPT)
    }

    override fun execute(): Response {
        val select = SqlQuerySelect(TFandoms.NAME,
                TFandoms.id,
                TFandoms.name,
                TFandoms.image_id,
                TFandoms.image_title_id,
                TFandoms.date_create,
                TFandoms.status)
        select.where(TFandoms.status, "=", API.STATUS_DRAFT)
        select.offset_count(offset, COUNT)
        select.sort(TFandoms.date_create, false)
        val v = Database.select("EFandomsSuggestedGetAll", select)
        val fandoms = arrayOfNulls<Fandom>(v.rowsCount)
        for (i in fandoms.indices) {
            fandoms[i] = Fandom()
            fandoms[i]!!.id = v.next<Long>()
            fandoms[i]!!.name = v.next()
            fandoms[i]!!.imageId = v.next<Long>()
            fandoms[i]!!.imageTitleId = v.next<Long>()
            fandoms[i]!!.dateCreate = v.next<Long>()
            fandoms[i]!!.status = v.next<Long>()
        }

        return Response(ToolsMapper.asNonNull(fandoms))
    }
}

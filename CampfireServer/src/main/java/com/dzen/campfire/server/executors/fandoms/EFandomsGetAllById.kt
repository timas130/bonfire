package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAllById
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlWhere

class EFandomsGetAllById : RFandomsGetAllById(emptyArray()) {

    @Throws(ApiException::class)
    override fun check() {

    }
    override fun execute(): Response {

        if(fandomsIds.isEmpty()){
            return Response(emptyArray())
        }

        return Response(ControllerFandom.parseSelect(Database.select("EFandomsGetAllById",ControllerFandom.instanceSelect()
                .where(TFandoms.status, "=", API.STATUS_PUBLIC)
                .where(SqlWhere.WhereIN(TFandoms.id, fandomsIds))
                .sort(TFandoms.subscribers_count, false))))
    }

}

package com.dzen.campfire.server.executors.rubrics

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.rubrics.RRubricsGetAll
import com.dzen.campfire.server.controllers.ControllerRubrics
import com.dzen.campfire.server.tables.TRubrics
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database

class ERubricsGetAll : RRubricsGetAll(0, 0, 0, 0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {

        val select = ControllerRubrics.instanceSelect()

        select.where(TRubrics.status, "=", API.STATUS_PUBLIC)
        if(fandomId > 0)select.where(TRubrics.fandom_id, "=", fandomId)
        if(languageId > 0)select.where(TRubrics.language_id, "=", languageId)
        if(ownerId > 0)select.where(TRubrics.owner_id, "=", ownerId)

        val v = Database.select("ERubricsGetAll", select
                .offset_count(offset, COUNT)
                .sort(TRubrics.date_create, false)
        )

        val array = ControllerRubrics.parseSelect(v)

        if(ownerId > 0){
            for(i in array){
                i.isWaitForPost = ControllerRubrics.isWaitForPostRubricsIds(ownerId, i.id)
            }
        }

         return Response(array)
    }

}

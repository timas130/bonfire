package com.dzen.campfire.server.executors.publications


import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.publications.RPublicationsGetAllDeepBlocked
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database

class EPublicationsGetAllDeepBlocked : RPublicationsGetAllDeepBlocked(0, 0) {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_PROTOADMIN)
    }

    override fun execute(): Response {
        val select = ControllerPublications.instanceSelect(apiAccount.id)
                .where(TPublications.status, "=", API.STATUS_DEEP_BLOCKED)
                .offset_count(offset, COUNT)
                .sort(TPublications.date_create, false)

        if(accountId > 0) select.where(TPublications.creator_id, "=", accountId)

        val publications = ControllerPublications.parseSelect(Database.select("EPublicationsGetAllDeepBlocked", select))
        ControllerPublications.loadSpecDataForPosts(apiAccount.id, publications)

        return Response(publications)
    }


}

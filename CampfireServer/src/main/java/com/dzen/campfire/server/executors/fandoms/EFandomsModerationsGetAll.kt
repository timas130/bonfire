package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationsGetAll
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database


class EFandomsModerationsGetAll : RFandomsModerationsGetAll(0, 0, 0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {

        val v = Database.select("EFandomsModerationsGetAll", ControllerPublications.instanceSelect(apiAccount.id)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_MODERATION)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.fandom_id, "=", fandomId)
                .where(TPublications.language_id, "=", languageId)
                .sort(TPublications.date_create, false)
                .offset_count(offset, COUNT))

        val publications = ControllerPublications.parseSelect(v)

        return Response(publications)
    }


}
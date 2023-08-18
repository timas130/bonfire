package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.moderations.PublicationModeration
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationGet
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database

class EFandomsModerationGet : RFandomsModerationGet(0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    @Throws(ApiException::class)
    override fun execute(): Response {

        val publications = ControllerPublications.parseSelect(Database.select("EFandomsModerationGet",
                ControllerPublications.instanceSelect(apiAccount.id)
                        .where(TPublications.id, "=", publicationId)))

        if (publications.isEmpty()) throw ApiException(API.ERROR_GONE)

        val publication = publications[0] as PublicationModeration

        return Response(publication)
    }
}

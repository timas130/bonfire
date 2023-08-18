package com.dzen.campfire.server.executors.tags

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.tags.RTagsGet
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database

class ETagsGet : RTagsGet(0) {

    @Throws(ApiException::class)
    override fun check() {
        super.check()
    }

    override fun execute(): Response {

        val select = ControllerPublications.instanceSelect(apiAccount.id)
        select.where(TPublications.id, "=", tagId)
        select.where(TPublications.status, "=", API.STATUS_PUBLIC)

        val v = Database.select("ETagsGet", select)

        val publications = ControllerPublications.parseSelect(v)

        return Response(publications[0] as PublicationTag)
    }
}

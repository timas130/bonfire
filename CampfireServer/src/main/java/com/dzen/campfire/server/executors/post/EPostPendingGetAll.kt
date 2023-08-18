package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostPendingGetAll
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database

class EPostPendingGetAll : RPostPendingGetAll(0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {

        val publications = ControllerPublications.parseSelect(
                Database.select("EPostPendingGetAll",
                        ControllerPublications.instanceSelect(apiAccount.id)
                                .where(TPublications.creator_id, "=", apiAccount.id)
                                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)
                                .where(TPublications.status, "=", API.STATUS_PENDING)
                                .offset_count(offset, COUNT)
                                .sort(TPublications.tag_4, false)
                )
        )

        return Response(Array(publications.size){publications[it] as PublicationPost})
    }
}

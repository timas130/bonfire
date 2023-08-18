package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostGetDraft
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlWhere

class EPostGetDraft : RPostGetDraft(0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    @Throws(ApiException::class)
    override fun execute(): Response {
        val publications = ControllerPublications.parseSelect(Database.select("EPostGetDraft",
                ControllerPublications.instanceSelect(apiAccount.id)
                        .where(TPublications.id, "=", publicationId)
                        .where(TPublications.creator_id, "=", apiAccount.id)
                        .where(SqlWhere.WhereIN(TPublications.status, arrayOf(API.STATUS_DRAFT, API.STATUS_PUBLIC, API.STATUS_PENDING)))
        ))

        if (publications.isEmpty()) throw ApiException(API.ERROR_GONE)

        val publication = publications[0] as PublicationPost

        return Response(publication, ControllerPublications.getTags(apiAccount.id, publication.id))
    }
}

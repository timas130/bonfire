package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistorySetNsfw
import com.dzen.campfire.api.requests.post.RPostSetNsfw
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerPublicationsHistory
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EPostSetNsfw : RPostSetNsfw(0, false) {
    override fun check() {
        val publication = ControllerPublications.getPublication(publicationId, apiAccount.id)
            ?: throw ApiException(API.ERROR_GONE)
        if (publication.status != API.STATUS_PUBLIC && publication.status != API.STATUS_DRAFT) throw ApiException(API.ERROR_GONE)
        if (publication.creator.id != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
        if (publication.publicationType != API.PUBLICATION_TYPE_POST) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {
        Database.update("EPostSetNsfw", SqlQueryUpdate(TPublications.NAME)
            .where(TPublications.id, "=", publicationId)
            .update(TPublications.nsfw, nsfw))
        ControllerPublicationsHistory.put(publicationId, HistorySetNsfw(apiAccount.id, apiAccount.imageId, apiAccount.name, nsfw))

        return Response()
    }
}

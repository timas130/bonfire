package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryCloseNo
import com.dzen.campfire.api.requests.post.RPostCloseNo
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerPublicationsHistory
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EPostCloseNo : RPostCloseNo(0) {


    override fun check() {
        val publication = ControllerPublications.getPublication(publicationId, apiAccount.id)
        if(publication == null) throw ApiException(API.ERROR_GONE)
        if(publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_GONE)
        if(publication.creator.id != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
        if(publication.publicationType != API.PUBLICATION_TYPE_POST) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        Database.update("EPostCloseNo", SqlQueryUpdate(TPublications.NAME).where(TPublications.id, "=", publicationId).update(TPublications.closed, 0))
        ControllerPublicationsHistory.put(publicationId, HistoryCloseNo(apiAccount.id, apiAccount.imageId, apiAccount.name))

        return Response()
    }


}

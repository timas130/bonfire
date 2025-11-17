package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryPublish
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostPendingPublish
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerPost
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerPublicationsHistory
import com.dzen.campfire.api.tools.ApiException

class EPostPendingPublish : RPostPendingPublish(0) {

    var publication = PublicationPost()

    override fun check() {
        val publication = ControllerPublications.getPublication(publicationId, apiAccount.id)

        if(publication == null)  throw ApiException(API.ERROR_GONE)
        ControllerAccounts.checkAccountBanned(apiAccount.id, publication.fandom.id, publication.fandom.languageId)
        if(publication.creator.id != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
        if(publication.status != API.STATUS_PENDING) throw ApiException(API.ERROR_ACCESS)
        if(publication !is PublicationPost) throw ApiException(API.ERROR_ACCESS)

        this.publication = publication


    }

    override fun execute(): Response {
        ControllerPost.publish(publicationId, publication.tag_3, publication.creator.id)

        ControllerPublicationsHistory.put(
            publicationId,
            HistoryPublish(apiAccount.id, apiAccount.imageId, apiAccount.name)
        )

        return Response()
    }

}

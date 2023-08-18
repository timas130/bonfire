package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryNotMultolingual
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostMakeMultilingualNot
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerPublicationsHistory
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerPost

class EPostMakeMultilingualNot : RPostMakeMultilingualNot(0) {

    private var publication = PublicationPost()

    override fun check() {
        ControllerAccounts.checkAccountBanned(apiAccount.id)

        publication = ControllerPublications.getPublication(publicationId, apiAccount.id) as PublicationPost
        if (publication.tag_5 < 1) throw ApiException(API.ERROR_ACCESS)
        if (publication.creator.id != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
        if (publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        if (publication.fandom.languageId > 0) throw ApiException(API.ERROR_ACCESS)

    }

    override fun execute(): Response {

        ControllerPost.setMultilingual(publication, false)
        ControllerPublicationsHistory.put(publication.id, HistoryNotMultolingual(apiAccount.id, apiAccount.imageId, apiAccount.name))

        return Response()
    }

}

package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostNotifyFollowers
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.api.tools.ApiException

class EPostNotifyFollowers : RPostNotifyFollowers(0) {

    var publication: PublicationPost? = null

    override fun check() {
        publication = ControllerPublications.getPublication(publicationId, apiAccount.id) as PublicationPost?

        if (publication == null) throw ApiException(API.ERROR_GONE)
        if (publication!!.status != API.STATUS_DRAFT && publication!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        if (publication!!.creator.id != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
        if (publication!!.tag_3 != 0L) throw ApiException(API.ERROR_ACCESS)

        ControllerAccounts.checkAccountBanned(apiAccount.id)
    }

    override fun execute(): Response {
        ControllerPublications.notifyFollowers(apiAccount, publication!!.id)
        return Response()
    }
}

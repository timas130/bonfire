package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryPinProfile
import com.dzen.campfire.api.models.publications.history.HistoryUnpinProfile
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostPinAccount
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerPublicationsHistory
import com.dzen.campfire.api.tools.ApiException

class EPostPinAccount : RPostPinAccount(0) {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_CAN_PIN_POST)

        if (postId > 0L) {
            val publication = ControllerPublications.getPublication(postId, apiAccount.id)
            if (publication == null || publication !is PublicationPost || publication.creator.id != apiAccount.id) throw ApiException(API.ERROR_GONE)
        }
    }

    override fun execute(): Response {

        if (postId > 0L) {
            ControllerCollisions.updateOrCreate(apiAccount.id, postId, API.COLLISION_ACCOUNT_PINNED_POST)
            ControllerPublicationsHistory.put(postId, HistoryPinProfile(apiAccount.id, apiAccount.imageId, apiAccount.name))
        } else {
            val oldPostId= ControllerCollisions.getCollision(apiAccount.id, API.COLLISION_ACCOUNT_PINNED_POST)
            ControllerCollisions.removeCollisions(apiAccount.id, API.COLLISION_ACCOUNT_PINNED_POST)
            if(oldPostId > 0)ControllerPublicationsHistory.put(oldPostId, HistoryUnpinProfile(apiAccount.id, apiAccount.imageId, apiAccount.name))
        }

        return Response()
    }
}

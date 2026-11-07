package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryAdminPinFandom
import com.dzen.campfire.api.models.publications.history.HistoryAdminUnpinFandom
import com.dzen.campfire.api.models.publications.moderations.posts.ModerationPinPostInFandom
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostPinFandom
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EPostPinFandom : RPostPinFandom(0, 0, 0, "") {

    override fun check() {
        ControllerModeration.parseComment(comment, apiAccount.id)

        if (postId > 0L) {
            val publication = ControllerPublications.getPublication(postId, apiAccount.id)
            if (publication == null) throw ApiException(API.ERROR_GONE)
            if (publication !is PublicationPost) throw ApiException(API.ERROR_GONE)
            if (publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_GONE)
            if (publication.fandom.id != fandomId) throw ApiException(API.ERROR_ACCESS)
            if (publication.fandom.languageId != -1L && publication.fandom.languageId != languageId) throw ApiException(API.ERROR_ACCESS)

            ControllerFandom.checkCan(apiAccount, publication.fandom.id, publication.fandom.languageId, API.LVL_MODERATOR_PIN_POST)
        } else {

            ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_PIN_POST)
        }
    }

    override fun execute(): Response {

        val pinnedPostId = ControllerCollisions.getCollisionValue1(fandomId, languageId, API.COLLISION_FANDOM_PINNED_POST)

        if (postId <= 0L && pinnedPostId <= 0L) return Response()

        // pinning the post that is already pinned does not replace anything
        val oldPostId = if (pinnedPostId == postId) 0L else pinnedPostId

        if (postId > 0L) {
            ControllerCollisions.updateOrCreateValue1(fandomId, languageId, API.COLLISION_FANDOM_PINNED_POST, postId)
            ControllerPublicationsHistory.put(postId, HistoryAdminPinFandom(apiAccount.id, apiAccount.imageId, apiAccount.name, comment))
        } else {
            ControllerCollisions.removeCollisions(fandomId, languageId, API.COLLISION_FANDOM_PINNED_POST)
            if (oldPostId > 0) ControllerPublicationsHistory.put(oldPostId, HistoryAdminUnpinFandom(apiAccount.id, apiAccount.imageId, apiAccount.name, comment))
        }

        ControllerPublications.moderation(ModerationPinPostInFandom(comment, postId, oldPostId), apiAccount.id, fandomId, languageId, postId)

        return Response()
    }
}

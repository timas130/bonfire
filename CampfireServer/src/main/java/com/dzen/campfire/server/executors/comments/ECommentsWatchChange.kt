package com.dzen.campfire.server.executors.comments

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.comments.RCommentsWatchChange
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.api.tools.ApiException

class ECommentsWatchChange : RCommentsWatchChange(0) {

    override fun check() {
        val publicationType = ControllerPublications.getType(publicationId)
        if (publicationType != API.PUBLICATION_TYPE_POST
                && publicationType != API.PUBLICATION_TYPE_MODERATION
                && publicationType != API.PUBLICATION_TYPE_STICKERS_PACK
        )
            throw ApiException(E_BAD_PUBLICATION_TYPE)
    }

    override fun execute(): Response {

        val follow = ControllerCollisions.checkCollisionExist(apiAccount.id, publicationId, API.COLLISION_COMMENTS_WATCH)
        ControllerPublications.watchComments(apiAccount.id, publicationId, !follow)

        return Response(!follow)
    }


}

package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.moderations.fandom.ModerationGalleryRemove
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationGalleryRemove
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EFandomsModerationGalleryRemove : RFandomsModerationGalleryRemove(0, 0, 0, "") {

    var fandom: Fandom? = null

    override fun check() {
        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_GALLERY)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        val collisions = ControllerCollisions.getCollisionsValue1(fandomId, languageId, API.COLLISION_FANDOM_GALLERY)

        var found = false
        for (id in collisions) if (id == imageId) found = true

        if (!found) throw ApiException(API.ERROR_GONE)

        ControllerCollisions.removeCollisionsValue1(fandomId, languageId, imageId, API.COLLISION_FANDOM_GALLERY)
        ControllerResources.remove(imageId)

        ControllerPublications.moderation(ModerationGalleryRemove(comment), apiAccount.id, fandomId, languageId, 0)

        return Response()
    }


}

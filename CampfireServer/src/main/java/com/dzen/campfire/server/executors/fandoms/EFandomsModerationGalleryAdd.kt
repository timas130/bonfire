package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.moderations.fandom.ModerationGalleryAdd
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationGalleryAdd
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.tools.ToolsImage

class EFandomsModerationGalleryAdd : RFandomsModerationGalleryAdd(0, 0, null, "") {

    var fandom: Fandom? = null

    override fun check() {
        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_GALLERY)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        if(ControllerCollisions.getCollisionsCount(fandomId,languageId, API.COLLISION_FANDOM_GALLERY) >= API.FANDOM_GALLERY_MAX)  throw ApiException(E_TOO_MANY_ITEMS)
        if(image == null || image!!.size > API.FANDOM_GALLERY_MAX_WEIGHT) throw ApiException(E_BAD_IMAGE)
        if(!ToolsImage.checkImageMaxScaleUnknownType(image!!, API.FANDOM_GALLERY_MAX_SIDE, API.FANDOM_GALLERY_MAX_SIDE, true, false, true)) throw ApiException(E_BAD_IMAGE)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        val imageId = ControllerResources.put(image, API.RESOURCES_PUBLICATION_FANDOM_GALLERY)
        ControllerCollisions.putCollisionValue1(fandomId, languageId, API.COLLISION_FANDOM_GALLERY, imageId)

        ControllerPublications.moderation(ModerationGalleryAdd(comment, imageId), apiAccount.id, fandomId, languageId, 0)
        ControllerCollisions.putCollisionWithCheck(apiAccount.id, 1, API.COLLISION_ACHIEVEMENT_VICEROY_IMAGES)
        ControllerAchievements.addAchievementWithCheck(
            ControllerViceroy.getViceroyId(fandomId, languageId),
            API.ACHI_VICEROY_IMAGES
        )

        return Response(imageId)
    }


}

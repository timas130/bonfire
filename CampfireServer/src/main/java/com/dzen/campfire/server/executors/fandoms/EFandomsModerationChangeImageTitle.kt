package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.moderations.fandom.ModerationTitleImage
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationChangeImageTitle
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.sup.dev.java_pc.tools.ToolsImage

class EFandomsModerationChangeImageTitle : RFandomsModerationChangeImageTitle(0, 0, null, null, "") {

    var fandom: Fandom? = null

    override fun check() {
        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_FANDOM_IMAGE)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)



        if (imageGif != null) {
            if (imageGif!!.size > API.FANDOM_TITLE_IMG_GIF_WEIGHT) throw ApiException(E_BAD_IMG_WEIGHT)
            if (!ToolsImage.checkImageMaxScaleUnknownType(imageGif!!, API.FANDOM_TITLE_IMG_GIF_W, API.FANDOM_TITLE_IMG_GIF_H, true, true, true)) throw ApiException(E_BAD_IMG_SIDES)
            if (!ToolsImage.checkImageMaxScaleUnknownType(image!!, API.FANDOM_TITLE_IMG_GIF_W, API.FANDOM_TITLE_IMG_GIF_H, true, false, true)) throw ApiException(E_BAD_IMG_SIDES)
        } else {
            if (image!!.size > API.FANDOM_TITLE_IMG_WEIGHT) throw ApiException(E_BAD_IMG_WEIGHT)
            if (!ToolsImage.checkImageMaxScaleUnknownType(image!!, API.FANDOM_TITLE_IMG_W, API.FANDOM_TITLE_IMG_H, true, false, true)) throw ApiException(E_BAD_IMG_SIDES)
        }
    }

    override fun execute(): Response {

        val oldCollisions = ControllerCollisions.getCollisionsValue1(fandomId, languageId, API.COLLISION_FANDOM_TITLE_IMAGE)
        val oldCollisionsGif = ControllerCollisions.getCollisionsValue3(fandomId, languageId, API.COLLISION_FANDOM_TITLE_IMAGE)

        val imageId = ControllerResources.put(image, API.RESOURCES_PUBLICATION_DATABASE_LINKED)
        var imageGifId = 0L
        if (imageGif != null) imageGifId = ControllerResources.put(imageGif, API.RESOURCES_PUBLICATION_DATABASE_LINKED)
        ControllerCollisions.removeCollisions(fandomId, languageId, API.COLLISION_FANDOM_TITLE_IMAGE)
        ControllerCollisions.putCollision(fandomId, languageId, null, API.COLLISION_FANDOM_TITLE_IMAGE, null, imageId, null, imageGifId, null, null)

        for (i in oldCollisions) ControllerResources.remove(i)
        for (i in oldCollisionsGif) ControllerResources.remove(i)

        ControllerPublications.moderation(ModerationTitleImage(comment, fandom!!.imageTitleId), apiAccount.id, fandomId, languageId, 0)

        return Response(imageId, imageGifId)
    }

}

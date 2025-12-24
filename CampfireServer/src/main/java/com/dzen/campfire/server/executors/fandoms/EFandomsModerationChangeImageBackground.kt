package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.moderations.fandom.ModerationBackgroundImage
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationChangeImageBackground
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.tools.ToolsImage

class EFandomsModerationChangeImageBackground : RFandomsModerationChangeImageBackground(0, 0, ByteArray(0), "") {


    var fandom: Fandom? = null

    override fun check() {
        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_BACKGROUND_IMAGE)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)

        comment = ControllerModeration.parseComment(comment, apiAccount.id)
        if(image != null) {
            if (image!!.size > API.CHAT_IMG_BACKGROUND_WEIGHT) throw ApiException(E_BAD_IMG_WEIGHT, " " + image!!.size + " > " + API.ACCOUNT_IMG_WEIGHT)
            if (!ToolsImage.checkImageMaxScaleUnknownType(image!!, API.CHAT_IMG_BACKGROUND_W, API.CHAT_IMG_BACKGROUND_H, true, true, true)) throw ApiException(E_BAD_IMG_SIDES)
        }
    }

    override fun execute(): Response {

        val oldCollisions = ControllerCollisions.getCollisionsValue1(fandomId, languageId, API.COLLISION_CHAT_BACKGROUND_IMAGE)

        val imageId = if (image == null) 0 else ControllerResources.put(image, API.RESOURCES_PUBLICATION_DATABASE_LINKED)
        ControllerCollisions.removeCollisions(fandomId, languageId, API.COLLISION_CHAT_BACKGROUND_IMAGE)
        if (imageId > 0)
            ControllerCollisions.putCollisionValue1(fandomId, languageId, API.COLLISION_CHAT_BACKGROUND_IMAGE, imageId)

        for (i in oldCollisions) ControllerResources.remove(i)

        ControllerPublications.moderation(ModerationBackgroundImage(comment, fandom!!.imageTitleId), apiAccount.id, fandomId, languageId, 0)

        return Response(imageId)
    }

}

package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.moderations.fandom.ModerationDescription
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationDescriptionChange
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EFandomsModerationDescriptionChange : RFandomsModerationDescriptionChange(0, 0, "", "") {

    var fandom: Fandom? = null

    override fun check() {
        description = ControllerCensor.cens(description)
        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_DESCRIPTION)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if(fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        if(description.length > API.FANDOM_DESCRIPTION_MAX_L) throw ApiException(E_BAD_TEXT_LENGTH)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        ControllerCollisions.removeCollisions(fandomId, languageId, API.COLLISION_FANDOM_DESCRIPTION)
        ControllerCollisions.putCollisionValue2(fandomId, languageId, API.COLLISION_FANDOM_DESCRIPTION, description)

        ControllerPublications.moderation(ModerationDescription(comment, description), apiAccount.id, fandomId, languageId, 0)

        ControllerCollisions.putCollisionWithCheck(apiAccount.id, 1, API.COLLISION_ACHIEVEMENT_VICEROY_DESCRIPTIONS)
        ControllerAchievements.addAchievementWithCheck(
            ControllerViceroy.getViceroyId(fandomId, languageId),
            API.ACHI_VICEROY_DESCRIPTION
        )

        return Response()
    }


}

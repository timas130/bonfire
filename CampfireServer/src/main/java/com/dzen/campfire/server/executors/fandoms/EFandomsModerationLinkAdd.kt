package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.fandoms.FandomLink
import com.dzen.campfire.api.models.publications.moderations.fandom.ModerationLinkAdd
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationLinkAdd
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EFandomsModerationLinkAdd : RFandomsModerationLinkAdd(0, 0, "", "", 0, "") {

    var fandom: Fandom? = null

    override fun check() {
        title = ControllerCensor.cens(title)
        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_LINKS)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        if(url.length > API.FANDOM_LINKS_URL_MAX_L) throw ApiException(E_BAD_SIZE)
        if(title.length > API.FANDOM_LINKS_TITLE_MAX_L) throw ApiException(E_BAD_SIZE)
        if(ControllerCollisions.getCollisionsCount(fandomId,languageId, API.COLLISION_FANDOM_LINK) >= API.FANDOM_LINKS_MAX)  throw ApiException(E_BAD_COUNT)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        val linkIndex = ControllerCollisions.putCollision(fandomId, languageId, 0, API.COLLISION_FANDOM_LINK, System.currentTimeMillis(), iconIndex, title+ FandomLink.SPLITER+url)

        ControllerPublications.moderation(ModerationLinkAdd(comment, title, url, iconIndex), apiAccount.id, fandomId, languageId, 0)
        ControllerCollisions.putCollisionWithCheck(apiAccount.id, 1, API.COLLISION_ACHIEVEMENT_VICEROY_LINK)
        ControllerAchievements.addAchievementWithCheck(
            ControllerViceroy.getViceroyId(fandomId, languageId),
            API.ACHI_VICEROY_LINK
        )

        return Response(linkIndex)
    }


}

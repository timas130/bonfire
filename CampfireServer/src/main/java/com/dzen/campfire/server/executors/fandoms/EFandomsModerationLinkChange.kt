package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.fandoms.FandomLink
import com.dzen.campfire.api.models.publications.moderations.fandom.ModerationLinkChange
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationLinkChange
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EFandomsModerationLinkChange : RFandomsModerationLinkChange(0, 0, 0, "", "", 0, "") {

    var fandom: Fandom? = null

    override fun check() {
        title = ControllerCensor.cens(title)
        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_LINKS)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        if (url.length > API.FANDOM_LINKS_URL_MAX_L) throw ApiException(E_BAD_SIZE)
        if (title.length > API.FANDOM_LINKS_TITLE_MAX_L) throw ApiException(E_BAD_SIZE)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        val update = SqlQueryUpdate(TCollisions.NAME)
                .where(TCollisions.id, "=", linkId)
                .where(TCollisions.owner_id, "=", fandomId)
                .where(TCollisions.collision_id, "=", languageId)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_LINK)
                .updateValue(TCollisions.value_1, iconIndex)
                .updateValue(TCollisions.value_2, title + FandomLink.SPLITER + url)

        Database.update("EFandomsModerationLinkChange", update)

        ControllerPublications.moderation(ModerationLinkChange(comment, title, url, iconIndex), apiAccount.id, fandomId, languageId, 0)

        return Response()
    }


}

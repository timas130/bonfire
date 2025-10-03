package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.moderations.fandom.ModerationLinkRemove
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationLinkRemove
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EFandomsModerationLinkRemove : RFandomsModerationLinkRemove(0, "") {

    var fandom: Fandom? = null

    override fun check() {

        val v = Database.select("EFandomsModerationLinkRemove", SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id, TCollisions.collision_id, TCollisions.collision_type)
                .where(TCollisions.id, "=", linkIndex))
        val fandomId: Long = v.next()
        val languageId: Long = v.next()
        val collisionType: Long = v.next()

        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_LINKS)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        if (collisionType != API.COLLISION_FANDOM_LINK) throw ApiException(E_BAD_TYPE)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        ControllerCollisions.removeById(linkIndex)
        ControllerPublications.moderation(ModerationLinkRemove(comment), apiAccount.id, fandom!!.id, fandom!!.languageId, 0)

        return Response()
    }


}

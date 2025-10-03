package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.fandom.NotificationForgive
import com.dzen.campfire.api.models.publications.moderations.publications.ModerationForgive
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationForgive
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TFandoms

class EFandomsModerationForgive : RFandomsModerationForgive(0, 0, 0, "") {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_BLOCK)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        ControllerCollisions.removeCollisions(accountId, fandomId, languageId, API.COLLISION_PUNISHMENTS_BAN)

        val v = ControllerAccounts.get(accountId, TAccounts.name, TAccounts.img_id)
        val accountName:String = v.next()
        val accountImageId:Long = v.next()

        val vv = ControllerFandom.get(fandomId, TFandoms.name, TFandoms.image_id)
        val fandomName:String = vv.next()
        val fandomImageId:Long = vv.next()

        ControllerPublications.moderation(ModerationForgive(comment, accountId, accountName, accountImageId), apiAccount.id, fandomId, languageId, 0)
        ControllerNotifications.push(accountId, NotificationForgive(fandomId, languageId, fandomImageId, fandomName, apiAccount.id, apiAccount.name, apiAccount.sex, comment))

        return Response()
    }


}

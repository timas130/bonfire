package com.dzen.campfire.server.executors.fandoms


import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.fandom.NotificationFandomRemoveModerator
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomRemoveModerator
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomRemoveModerator
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserFandomRemoveModerator
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminRemoveModerator
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.debug.log

class EFandomsAdminRemoveModerator : RFandomsAdminRemoveModerator(0, 0, 0, "") {

    var karma30 = 0L

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_REMOVE_MODERATOR)
        karma30 = ControllerFandom.getKarma30(accountId, fandomId, languageId)
        if (karma30 < API.LVL_MODERATOR_BLOCK.karmaCount) throw ApiException(E_NOT_MODERATOR)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        val vF = ControllerFandom.get(fandomId, TFandoms.image_id, TFandoms.name)
        val fandomImageId: Long = vF.next()
        val fandomName: String = vF.next()
        val vA = ControllerAccounts.get(accountId, TAccounts.name, TAccounts.img_id, TAccounts.sex)
        val tName: String = vA.next()
        val tImageId: Long = vA.next()
        val tSex: Long = vA.next()

        ControllerKarma.addKarmaTransaction(apiAccount, -karma30, 100, true, accountId, fandomId, languageId, 0, false)
        ControllerKarma.recountKarma30(accountId)

        ControllerNotifications.push(accountId, NotificationFandomRemoveModerator(fandomImageId, fandomId, languageId, fandomName, comment))
        ControllerPublications.event(ApiEventAdminFandomRemoveModerator(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, accountId, tName, tImageId, tSex, comment, fandomId, languageId, fandomImageId, fandomName), apiAccount.id)
        ControllerPublications.event(ApiEventFandomRemoveModerator(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, accountId, tName, tImageId, tSex, fandomId, languageId, fandomName, fandomImageId, comment), apiAccount.id, fandomId, languageId)
        ControllerPublications.event(ApiEventUserFandomRemoveModerator(accountId, tName, tImageId, tSex, apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, fandomId, languageId, fandomImageId, fandomName), accountId)

        return Response()
    }


}

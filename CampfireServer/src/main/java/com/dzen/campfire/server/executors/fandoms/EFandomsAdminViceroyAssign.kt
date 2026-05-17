package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.notifications.fandom.NotificationFandomViceroyAssign
import com.dzen.campfire.api.models.notifications.fandom.NotificationFandomViceroyRemove
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomViceroyAssign
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomViceroyAssign
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminViceroyAssign
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminViceroyRemove
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminViceroyAssign
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EFandomsAdminViceroyAssign : RFandomsAdminViceroyAssign(0, 0, 0, "") {

    var fandom = Fandom()
    var account = Account()

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_VICEROY)
        val fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)

        fandom.languageId = languageId
        this.fandom = fandom

        val account = ControllerAccounts.getAccount(accountId)
        if (account == null) throw ApiException(API.ERROR_GONE)
        this.account = account
    }

    override fun execute(): Response {

        val oldAccountId = ControllerCollisions.getCollisionValue1(fandom.id, fandom.languageId, API.COLLISION_FANDOM_VICEROY)
        ControllerCollisions.removeCollisions(fandom.id, fandom.languageId, API.COLLISION_FANDOM_VICEROY)
        ControllerCollisions.putCollisionValue1(fandom.id, fandom.languageId, 0, API.COLLISION_FANDOM_VICEROY, System.currentTimeMillis(), accountId)

        val eAdmin = ApiEventAdminFandomViceroyAssign(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, oldAccountId, "", accountId, account.name, fandom.id, fandom.name,  fandom.imageId)
        val eFandom = ApiEventFandomViceroyAssign(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, fandom.id, fandom.name, fandom.imageId, comment, oldAccountId, "", account.id, account.name)
        val eAccount = ApiEventUserAdminViceroyAssign(account.id, account.name, account.imageId, account.sex, apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, fandom.id, fandom.imageId, fandom.name, fandom.imageId)
        val nAccount = NotificationFandomViceroyAssign(fandom.imageId, fandom.id, fandom.languageId, fandom.name, comment, apiAccount.id, apiAccount.name, apiAccount.sex, oldAccountId, "", account.id, account.name)

        if (oldAccountId > 0) {
            val oldAccount = ControllerAccounts.getAccount(oldAccountId)
            if (oldAccount != null) {
                ControllerPublications.event(ApiEventUserAdminViceroyRemove(oldAccount.id, oldAccount.name, oldAccount.imageId, oldAccount.sex, apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, fandom.id, fandom.languageId, fandom.name, fandom.imageId), oldAccount.id)
                eAdmin.oldAccountName = oldAccount.name
                eFandom.oldAccountName = oldAccount.name
                nAccount.oldAccountName = oldAccount.name
                ControllerNotifications.push(oldAccount.id, NotificationFandomViceroyRemove(fandom.imageId, fandom.id, fandom.languageId, fandom.name, comment, apiAccount.id, apiAccount.name, apiAccount.sex, oldAccount.id, oldAccount.name))
            }
        }

        ControllerPublications.event(eAdmin, apiAccount.id)
        ControllerPublications.event(eAccount, account.id)
        ControllerPublications.event(eFandom, apiAccount.id, fandom.id, fandom.languageId)
        ControllerNotifications.push(account.id, nAccount)

        ControllerCollisions.putCollisionWithCheck(accountId, 1, API.COLLISION_ACHIEVEMENT_VICEROY_ASSIGN)
        ControllerAchievements.addAchievementWithCheck(accountId, API.ACHI_VICEROY_ASSIGN)

        return Response()
    }

}

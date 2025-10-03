package com.dzen.campfire.server.executors.fandoms


import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.notifications.fandom.NotificationFandomViceroyRemove
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomViceroyRemove
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomViceroyRemove
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminViceroyRemove
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminViceroyRemove
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EFandomsAdminViceroyRemove : RFandomsAdminViceroyRemove(0, 0, "") {

    var fandom = Fandom()

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_VICEROY)
        val fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)

        fandom.languageId = languageId
        this.fandom = fandom
    }

    override fun execute(): Response {

        val oldAccountId = ControllerCollisions.getCollisionValue1(fandom.id, fandom.languageId, API.COLLISION_FANDOM_VICEROY)
        ControllerCollisions.removeCollisions(fandom.id, fandom.languageId, API.COLLISION_FANDOM_VICEROY)

        if (oldAccountId > 0) {
            val oldAccount = ControllerAccounts.getAccount(oldAccountId)
            if (oldAccount != null) {


                ControllerPublications.event(ApiEventUserAdminViceroyRemove(oldAccount.id, oldAccount.name, oldAccount.imageId, oldAccount.sex, apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, fandom.id, fandom.languageId, fandom.name, fandom.imageId), oldAccount.id)
                ControllerPublications.event(ApiEventAdminFandomViceroyRemove(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, oldAccount.id, oldAccount.name, fandom.id, fandom.name, fandom.imageId), apiAccount.id)
                ControllerPublications.event(ApiEventFandomViceroyRemove(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, fandom.id, fandom.name, fandom.imageId, comment, oldAccount.id, oldAccount.name), apiAccount.id, fandom.id, fandom.languageId)
                ControllerNotifications.push(oldAccount.id, NotificationFandomViceroyRemove(fandom.imageId, fandom.id, fandom.languageId, fandom.name, comment, apiAccount.id, apiAccount.name, apiAccount.sex, oldAccount.id, oldAccount.name))
            }
        }


        return Response()
    }

}

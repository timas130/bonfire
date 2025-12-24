package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.notifications.fandom.NotificationFandomAccepted
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomAccepted
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomAccepted
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserFandomSuggest
import com.dzen.campfire.api.requests.fandoms.RFandomsAccept
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EFandomsAccept : RFandomsAccept(0, false, "") {

    companion object {

        val SUNC = "SUNC"

    }

    private var fandom: Fandom = Fandom()

    @Throws(ApiException::class)
    override fun check() {
        synchronized(SUNC) {
            ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOMS_ACCEPT)
            val fandom = ControllerFandom.getFandom(fandomId)
            if (fandom == null) throw ApiException(API.ERROR_GONE)
            if (fandom.status != API.STATUS_DRAFT) throw ApiException(E_BAD_STATUS)
            if (fandom.creatorId == apiAccount.id) throw ApiException(E_SELF)
            if (!accepted) {
                comment = ControllerModeration.parseComment(comment, apiAccount.id)
            }
            this.fandom = fandom
        }
    }

    override fun execute(): Response {

        synchronized(SUNC) {

            Database.update("EFandomsAccept", SqlQueryUpdate(TFandoms.NAME)
                    .where(TFandoms.id, "=", fandomId)
                    .update(TFandoms.status, if (accepted) API.STATUS_PUBLIC else API.STATUS_BLOCKED))

            ControllerNotifications.push(fandom.creatorId, NotificationFandomAccepted(fandom.creatorId, fandom.imageId, fandomId, fandom.name, accepted, comment, apiAccount.name))

            if (accepted) {
                ControllerAchievements.addAchievementWithCheck(fandom.creatorId, API.ACHI_FANDOMS)


                val v = ControllerAccounts.get(fandom.creatorId, TAccounts.name, TAccounts.img_id, TAccounts.sex)
                val creatorAccountName: String = v.next()
                val creatorAccountImageId: Long = v.next()
                val creatorAccountSex: Long = v.next()

                ControllerPublications.event(ApiEventUserFandomSuggest(fandom.creatorId, creatorAccountName, creatorAccountImageId, creatorAccountSex, fandom.id, fandom.name, fandom.imageId), fandom.creatorId)
                ControllerPublications.event(ApiEventAdminFandomAccepted(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, fandom.id, fandom.name, fandom.imageId), fandom.creatorId)
                ControllerPublications.event(ApiEventFandomAccepted(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, fandom.id, fandom.name, fandom.imageId, comment), apiAccount.id, fandom.id, 0)

                ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_ACCEPT_FANDOM)
                ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_ACCEPT_FANDOM)

            }

        }

        return Response()
    }
}

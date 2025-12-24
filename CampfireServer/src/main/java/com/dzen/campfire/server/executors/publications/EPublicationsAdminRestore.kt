package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.fandom.NotificationModerationRejected
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationRestore
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminModerationRejected
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminPublicationRestore
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminModerationRejected
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminPublicationRestored
import com.dzen.campfire.api.models.publications.history.HistoryAdminNotBlock
import com.dzen.campfire.api.models.publications.moderations.PublicationModeration
import com.dzen.campfire.api.models.publications.moderations.publications.ModerationBlock
import com.dzen.campfire.api.requests.publications.RPublicationsAdminRestore
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EPublicationsAdminRestore : RPublicationsAdminRestore(0, "", true) {

    private var publicationModeration: Publication? = null
    private var publication: Publication? = null

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_ADMIN)
        publicationModeration = ControllerPublications.getPublication(moderationId, apiAccount.id)
        if (publicationModeration == null) throw ApiException(API.ERROR_GONE)
        if (publicationModeration !is PublicationModeration || (publicationModeration as PublicationModeration).moderation !is ModerationBlock) throw ApiException(E_BAD_MODERATION_TYPE)
        if (apiAccount.id != 1L && publicationModeration!!.creator.id == apiAccount.id) throw ApiException(E_SELF)
        publication = ControllerPublications.getPublication(((publicationModeration as PublicationModeration).moderation as ModerationBlock).publicationId, apiAccount.id)
        if (apiAccount.id != 1L && publication!!.creator.id == apiAccount.id) throw ApiException(E_SELF)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        if (publication != null) {
            Database.update("EPublicationsAdminRestore update_1",SqlQueryUpdate(TPublications.NAME)
                    .where(TPublications.id, "=", publication!!.id)
                    .update(TPublications.status, API.STATUS_PUBLIC))
            ControllerPublications.clearReports(publication!!.id)

            val v = ControllerAccounts.get(publication!!.creator.id, TAccounts.name, TAccounts.img_id, TAccounts.sex)
            val accountName: String = v.next()
            val accountImageId: Long = v.next()
            val accountSex: Long = v.next()

            ControllerPublications.event(ApiEventAdminPublicationRestore(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, publication!!.creator.id, accountName, accountImageId, accountSex, comment, publication!!.id, publicationModeration!!.id), apiAccount.id)

            val notificationBlock = NotificationPublicationRestore(publication!!.id, publication!!.parentPublicationId, publication!!.parentPublicationType, publication!!.fandom.imageId, comment, publication!!.publicationType)
            ControllerNotifications.push(publication!!.creator.id, notificationBlock)
        }

        if (vahter) ControllerVahter.addAdminRejected(publicationModeration!!.creator.id)
        ControllerAccounts.removePunishment(ControllerAccounts.getAccount(apiAccount.id)!!, comment, publicationModeration!!.tag_3)

        ((publicationModeration!! as PublicationModeration).moderation as ModerationBlock).checkAdminId = apiAccount.id
        ((publicationModeration!! as PublicationModeration).moderation as ModerationBlock).checkAdminName = apiAccount.name
        ((publicationModeration!! as PublicationModeration).moderation as ModerationBlock).checkAdminComment = comment

        Database.update("EPublicationsAdminRestore update_3",SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.id, "=", publicationModeration!!.id)
                .update(TPublications.tag_2, 2)
                .updateValue(TPublications.publication_json, publicationModeration!!.jsonDB(true, Json())))



        val v = ControllerAccounts.get(publicationModeration!!.creator.id, TAccounts.name, TAccounts.img_id, TAccounts.sex)
        val moderatorName: String = v.next()
        val moderatorImageId: Long = v.next()
        val moderatorSex: Long = v.next()

        ControllerPublications.event(ApiEventAdminModerationRejected(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, publicationModeration!!.creator.id, moderatorName, moderatorImageId, moderatorSex, comment, publicationModeration!!.id), apiAccount.id)
        ControllerPublications.event(ApiEventUserAdminModerationRejected(publicationModeration!!.creator.id, moderatorName, moderatorImageId, moderatorSex, apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, publicationModeration!!.id, publication!!.fandom.name), publicationModeration!!.creator.id)
        ControllerPublications.event(ApiEventUserAdminPublicationRestored(publication!!.creator.id, publication!!.creator.name, publication!!.creator.imageId, publication!!.creator.sex, apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, publicationModeration!!.id), publication!!.creator.id)

        val notificationBlock = NotificationModerationRejected(publicationModeration!!.id, publicationModeration!!.fandom.id, publicationModeration!!.fandom.languageId, publicationModeration!!.fandom.imageId, comment, apiAccount.name, apiAccount.sex)
        ControllerNotifications.push(publicationModeration!!.creator.id, notificationBlock)

        ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_REVIEW_MODER_ACTION)
        ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_REVIEW_MODER_ACTION)

        ControllerPublicationsHistory.put(publication!!.id, HistoryAdminNotBlock(apiAccount.id, apiAccount.imageId, apiAccount.name, comment))

        return Response()
    }


}

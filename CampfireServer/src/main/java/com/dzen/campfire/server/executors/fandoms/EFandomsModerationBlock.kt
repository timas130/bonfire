package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.AccountPunishment
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationBlock
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationBlockAfterReport
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminBlockPublication
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminPublicationBlocked
import com.dzen.campfire.api.models.publications.history.HistoryAdminBlock
import com.dzen.campfire.api.models.publications.moderations.publications.ModerationBlock
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationBlock
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.sql.SqlWhere

class EFandomsModerationBlock : RFandomsModerationBlock(0, 0, false, "", false, 0) {

    private var publication: Publication? = null
    private var blockAccountDate = 0L
    private var punishmentId = 0L
    private var languageId = userLanguageId
    private var publicationChatMessage: PublicationChatMessage? = null

    override fun check() {
        publication = ControllerPublications.getPublication(publicationId, apiAccount.id)
        if (publication == null) throw ApiException(API.ERROR_GONE)
        if (publication!!.creator.id == apiAccount.id) throw ApiException(E_SELF_PUBLICATION)
        if (publication!!.status == API.STATUS_DRAFT) throw ApiException(E_DRAFT)
        if (publication!!.status != API.STATUS_PUBLIC) throw ApiException(E_ALREADY)
        if (publication!!.publicationType == API.PUBLICATION_TYPE_QUEST) throw ApiException(API.ERROR_ACCESS)

        languageId = publication!!.fandom.languageId
        if (publication!!.fandom.languageId == -1L && publication!!.publicationType == API.PUBLICATION_TYPE_POST) languageId = userLanguageId

        ControllerFandom.checkCan(
                apiAccount,
                publication!!.fandom.id,
                languageId,
                API.LVL_MODERATOR_BLOCK
        )
        if (blockInApp) ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_BAN)
        if (!ControllerFandom.checkCanModerate(
                        apiAccount,
                        publication!!.creator.id,
                        publication!!.fandom.id,
                        languageId
                )
        ) throw ApiException(E_LOW_KARMA_FORCE)
        if (blockTime > 1000L * 60 * 60 * 24 * 365) throw ApiException(API.ERROR_ACCESS)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)

        val canBlockTag = ControllerVahter.isCanBlock(apiAccount.id)
        if(canBlockTag != -1L){
            if(canBlockTag == API.EFFECT_COMMENT_TAG_REJECTED) throw ApiException(E_BLOCKS_REJECTED_STATE)
            throw ApiException(E_BLOCKS_LIMIT)
        }
    }

    override fun execute(): Response {
        ControllerVahter.addAdminPublicationBlock(apiAccount.id)
        val ids = ArrayList<Long>()

        ControllerPublications.changeStatus(publicationId, if (publication!!.fandom.id > 0) API.STATUS_BLOCKED else API.STATUS_DEEP_BLOCKED)
        if (publication!!.publicationType == API.PUBLICATION_TYPE_COMMENT) {
            Database.update("EFandomsModerationBlock update_2",
                    SqlQueryUpdate(TPublications.NAME).where(
                            TPublications.id,
                            "=",
                            publication!!.parentPublicationId
                    ).update(TPublications.subpublications_count, TPublications.subpublications_count + "-1")
            )
        }

        ids.add(publicationId)

        if (blockLastPublications) blockLastPublications(ids)
        if (publication!!.publicationType == API.PUBLICATION_TYPE_COMMENT) {
            ControllerPublications.recountBestComment(publication!!.parentPublicationId, publication!!.id)
            ControllerAccounts.updateCommentsCount(apiAccount.id, if (blockLastPublications) 0 else -1)
        }
        if (publication!!.publicationType == API.PUBLICATION_TYPE_CHAT_MESSAGE) ControllerChats.onMessagesRemoved((publication!! as PublicationChatMessage).chatTag(), 1)
        if (publication!!.publicationType == API.PUBLICATION_TYPE_POST) ControllerAccounts.updatePostsCount(apiAccount.id, if (blockLastPublications) 0 else -1)

        if (publication!!.publicationType == API.PUBLICATION_TYPE_STICKERS_PACK) ControllerStickers.removeCollisionsStickersPack(publication!!.id)
        if (publication!!.publicationType == API.PUBLICATION_TYPE_STICKER) ControllerStickers.removeCollisionsSticker(publication!!.id)



        if (blockTime > 0L) blockAccountDate = System.currentTimeMillis() + blockTime

        val moderationId = createModerationAndEvents()

        if (blockTime > 0L) {
            if (blockInApp || moderationId == 0L) {
                banAccountInApp()
            } else {
                banAccountInFandom()
            }
            if (publication!!.publicationType == API.PUBLICATION_TYPE_CHAT_MESSAGE) postChatMessage(moderationId)
        } else if (blockTime == -1L) {
            warnAccount()
            if (publication!!.publicationType == API.PUBLICATION_TYPE_CHAT_MESSAGE) postChatMessage(moderationId)
        }

        Database.update("EFandomsModerationBlock update_3", SqlQueryUpdate(TPublications.NAME).where(TPublications.id, "=", moderationId).update(TPublications.tag_3, punishmentId))

        ControllerNotifications.push(publication!!.creator.id, NotificationPublicationBlock(blockLastPublications, blockAccountDate, comment, moderationId, publication!!.publicationType, publication!!.tag_s_1))
        notificationByReports(moderationId)

        return Response(ToolsMapper.asArray(ids), publicationChatMessage)
    }

    private fun postChatMessage(moderationId: Long) {
        val tag = ChatTag(API.CHAT_TYPE_FANDOM_ROOT, publication!!.fandom.id, publication!!.fandom.languageId)

        publicationChatMessage = PublicationChatMessage()
        publicationChatMessage!!.type = PublicationChatMessage.TYPE_SYSTEM
        publicationChatMessage!!.systemType = PublicationChatMessage.SYSTEM_TYPE_BLOCK
        publicationChatMessage!!.systemOwnerId = apiAccount.id
        publicationChatMessage!!.systemOwnerSex = apiAccount.sex
        publicationChatMessage!!.systemOwnerName = apiAccount.name
        publicationChatMessage!!.systemTargetName = publication!!.creator.name
        publicationChatMessage!!.systemComment = comment
        publicationChatMessage!!.blockModerationEventId = moderationId
        publicationChatMessage!!.blockDate = blockAccountDate
        publicationChatMessage!!.category = publication!!.category

        ControllerChats.putMessage(apiAccount, publicationChatMessage!!, tag)
    }

    private fun banAccountInApp() {
        punishmentId = ControllerAccounts.ban(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, publication!!.creator.id, blockTime, comment, false)
    }

    private fun banAccountInFandom() {
        punishmentId = ControllerCollisions.putCollision(publication!!.creator.id, publication!!.fandom.id, languageId, API.COLLISION_PUNISHMENTS_BAN, System.currentTimeMillis(), blockAccountDate, AccountPunishment.createSupportString(comment, apiAccount.id, apiAccount.imageId, apiAccount.name, apiAccount.sex, blockAccountDate))
        ControllerActivities.dropActivities(publication!!.creator.id, publication!!.fandom.id, languageId)
    }

    private fun warnAccount() {
        punishmentId = ControllerCollisions.putCollision(publication!!.creator.id, publication!!.fandom.id, languageId, API.COLLISION_PUNISHMENTS_WARN, System.currentTimeMillis(), 0, AccountPunishment.createSupportString(comment, apiAccount.id, apiAccount.imageId, apiAccount.name, apiAccount.sex, -1))
    }

    private fun blockLastPublications(ids: ArrayList<Long>) {
        val v = Database.select("EFandomsModerationBlock.blockLastPublications select",
                SqlQuerySelect(TPublications.NAME, TPublications.id, TPublications.publication_type, TPublications.parent_publication_id, TPublications.tag_1, TPublications.tag_2, TPublications.tag_3)
                        .where(TPublications.fandom_id, "=", publication!!.fandom.id)
                        .where(TPublications.language_id, "=", languageId)
                        .where(TPublications.creator_id, "=", publication!!.creator.id)
                        .where(TPublications.date_create, ">", publication!!.dateCreate - 1000L * 60 * 60)
                        .where(TPublications.date_create, "<=", publication!!.dateCreate)
                        .where(TPublications.status, "=", API.STATUS_PUBLIC)
                        .where(
                                SqlWhere.WhereIN(
                                        TPublications.publication_type,
                                        arrayOf(API.PUBLICATION_TYPE_POST, API.PUBLICATION_TYPE_COMMENT, API.PUBLICATION_TYPE_CHAT_MESSAGE)
                                )
                        )
        )
        for (i in 0 until v.rowsCount) {
            val id: Long = v.next()
            val type: Long = v.next()
            val parentPublicationId: Long = v.next()
            val tag1: Long = v.next()
            val tag2: Long = v.next()
            val tag3: Long = v.next()
            if (!ids.contains(id)) {
                ids.add(id)
                ControllerPublications.changeStatus(id, API.STATUS_BLOCKED)
                if (type == API.PUBLICATION_TYPE_COMMENT) {
                    Database.update("EFandomsModerationBlock.blockLastPublications update",
                            SqlQueryUpdate(TPublications.NAME).where(
                                    TPublications.id,
                                    "=",
                                    parentPublicationId
                            ).update(TPublications.subpublications_count, TPublications.subpublications_count + "-1")
                    )
                }
                if (type == API.PUBLICATION_TYPE_CHAT_MESSAGE) {
                    ControllerChats.onMessagesRemoved(ChatTag(tag1, tag2, tag3), 1)
                }
            }
        }
    }

    private fun createModerationAndEvents(): Long {

        var moderationId = 0L

        if (publication!!.publicationType == API.PUBLICATION_TYPE_STICKERS_PACK) {
            ControllerPublications.event(ApiEventAdminBlockPublication(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, publication!!.creator.id, publication!!.creator.name, publication!!.creator.imageId, publication!!.creator.sex, comment, publication!!.getPublicationTypeConst(), blockAccountDate, blockLastPublications, blockTime == -1L, true, "", 0, 0), apiAccount.id)
            ControllerPublications.event(ApiEventUserAdminPublicationBlocked(publication!!.creator.id, publication!!.creator.name, publication!!.creator.imageId, publication!!.creator.sex, apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, publication!!.getPublicationTypeConst(), 0, blockAccountDate, blockLastPublications, blockTime == -1L, true, "", 0, 0), publication!!.creator.id)
        }

        if (publication!!.publicationType == API.PUBLICATION_TYPE_POST
                || publication!!.publicationType == API.PUBLICATION_TYPE_COMMENT
                || publication!!.publicationType == API.PUBLICATION_TYPE_CHAT_MESSAGE) {
            moderationId = ControllerPublications.moderation(ModerationBlock(comment, publicationId, publication!!.getPublicationTypeConst(), publication!!.creator.id, publication!!.creator.name, publication!!.creator.imageId, blockAccountDate, blockLastPublications), apiAccount.id, publication!!.fandom.id, languageId, publication!!.id)
            ControllerPublications.event(ApiEventUserAdminPublicationBlocked(publication!!.creator.id, publication!!.creator.name, publication!!.creator.imageId, publication!!.creator.sex, apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, publication!!.getPublicationTypeConst(), moderationId, blockAccountDate, blockLastPublications, blockTime == -1L, blockInApp, publication!!.fandom.name, publication!!.fandom.id, publication!!.fandom.languageId), publication!!.creator.id)
        }

        ControllerPublicationsHistory.put(publicationId, HistoryAdminBlock(apiAccount.id, apiAccount.imageId, apiAccount.name, comment))

        return moderationId
    }

    private fun notificationByReports(moderationId: Long) {
        val v = Database.select("EFandomsModerationBlock.notificationByReports", SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
                .where(TCollisions.collision_type, "=", API.COLLISION_PUBLICATION_REPORT)
                .where(TCollisions.owner_id, "=", publicationId))

        var accountIds = Array(v.rowsCount) { v.next<Long>() }
        accountIds = ToolsCollections.removeItem(apiAccount.id, accountIds)

        ControllerNotifications.push(accountIds, NotificationPublicationBlockAfterReport(blockLastPublications, blockAccountDate, comment, moderationId, publication!!.publicationType, publication!!.tag_s_1))

    }

}

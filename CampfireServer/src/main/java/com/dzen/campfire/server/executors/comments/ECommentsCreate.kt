package com.dzen.campfire.server.executors.comments

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.models.publications.history.HistoryCreate
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.requests.comments.RCommentsCreate
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.sql.SqlWhere
import com.sup.dev.java_pc.tools.ToolsImage

class ECommentsCreate : RCommentsCreate(0, "", null, null, 0, false, 0, 0, false) {

    private var sticker = PublicationSticker()
    private val comment = PublicationComment()
    private var publication: Publication? = null
    private var parentComment = PublicationComment()

    override fun check() {
        text = text.trim { it <= ' ' }
        text = ControllerCensor.cens(text)

        if ((stickerId <= 0 && imageArray == null && text.length < API.COMMENT_MIN_L) || text.length > API.COMMENT_MAX_L) throw ApiException(E_BAD_TEXT_SIZE)
        if (stickerId <= 0 && imageArray == null && text.isEmpty()) throw ApiException(E_BAD_DATA)
        if (gif != null && (imageArray == null || imageArray!!.size != 1)) throw ApiException(E_BAD_DATA)
        if (imageArray != null && imageArray!!.isEmpty()) throw ApiException(E_BAD_DATA)

        publication = ControllerPublications.getPublication(publicationId, apiAccount.id)
        if (publication == null) throw ApiException(API.ERROR_GONE)

        if (publication!!.publicationType != API.PUBLICATION_TYPE_POST
                && publication!!.publicationType != API.PUBLICATION_TYPE_MODERATION
                && publication!!.publicationType != API.PUBLICATION_TYPE_STICKERS_PACK
                && publication!!.publicationType != API.PUBLICATION_TYPE_QUEST
        )
            throw ApiException(E_BAD_PUBLICATION_TYPE)

        if (publication!!.status != API.STATUS_PUBLIC)
            throw ApiException(E_BAD_PUBLICATION_STATUS)

        if (parentCommentId != 0L) {
            val parentCommentX = ControllerPublications.getPublication(parentCommentId, apiAccount.id) as PublicationComment?
            if (parentCommentX != null) {
                parentComment = parentCommentX
                if (parentCommentX.publicationType != API.PUBLICATION_TYPE_COMMENT) throw ApiException(E_BAD_PARENT_COMMENT_TYPE)
                if (parentCommentX.parentPublicationId != publicationId) throw ApiException(E_BAD_PARENT_COMMENT_TARGET_PUBLICATION_ID)
            }
        }
        if (imageArray != null) {
            if (imageArray!!.size > 1) {
                comment.imageWArray = Array(imageArray!!.size) { 0 }
                comment.imageHArray = Array(imageArray!!.size) { 0 }
            }
            for (i in 0 until imageArray!!.size) {
                if (!ToolsBytes.isGif(imageArray!![i]) && imageArray!![i].size > API.CHAT_MESSAGE_IMAGE_WEIGHT) throw ApiException(E_BAD_IMAGE)
                if (ToolsBytes.isGif(imageArray!![i]) && imageArray!![i].size > API.CHAT_MESSAGE_GIF_MAX_WEIGHT) throw ApiException(E_BAD_IMAGE)
                val scale = ToolsImage.getImgScaleUnknownType(imageArray!![i], true, true, true)
                comment.imageW = scale[0]
                comment.imageH = scale[1]
                if (imageArray!!.size > 1) {
                    comment.imageWArray[i] = scale[0]
                    comment.imageHArray[i] = scale[1]
                }
                if (!ToolsBytes.isGif(imageArray!![i]) && (scale[0] > API.CHAT_MESSAGE_IMAGE_SIDE || scale[1] > API.CHAT_MESSAGE_IMAGE_SIDE)) throw ApiException(E_BAD_IMAGE)
                if (ToolsBytes.isGif(imageArray!![i]) && (scale[0] > API.CHAT_MESSAGE_IMAGE_SIDE_GIF || scale[1] > API.CHAT_MESSAGE_IMAGE_SIDE_GIF)) throw ApiException(E_BAD_IMAGE)
            }
        }
        if (gif != null) {
            if (!ToolsBytes.isGif(gif)) throw ApiException(E_BAD_GIF)
            if (gif!!.size > API.CHAT_MESSAGE_GIF_MAX_WEIGHT) throw ApiException(E_BAD_GIF)
            val scale = ToolsImage.getImgScaleUnknownType(gif!!, true, true, true)
            if (scale[0] > API.CHAT_MESSAGE_IMAGE_SIDE_GIF || scale[1] > API.CHAT_MESSAGE_IMAGE_SIDE_GIF) throw ApiException(E_BAD_IMAGE)
            val scale2 = ToolsImage.getImgScaleUnknownType(imageArray!![0], true, false, true)
            if (scale2[0] > API.CHAT_MESSAGE_IMAGE_SIDE_GIF || scale2[1] > API.CHAT_MESSAGE_IMAGE_SIDE_GIF) throw ApiException(E_BAD_IMAGE)
        }
        if (stickerId != 0L) {
            sticker = ControllerPublications.getPublication(stickerId, apiAccount.id) as PublicationSticker
            if (sticker.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        }

        ControllerAccounts.checkAccountBanned(apiAccount.id, publication!!.fandom.id, publication!!.fandom.languageId)
    }


    override fun execute(): Response {

        comment.text = text
        if (gif != null) {
            parseGif()
        } else if (imageArray != null) {
            parseImage()
        } else if (stickerId != 0L) {
            parseSticker()
        } else {
            comment.type = PublicationComment.TYPE_TEXT
        }
        comment.newFormatting = newFormatting

        if (quoteId != 0L) {
            val quotePublication = ControllerPublications.getPublication(quoteId, apiAccount.id)
            if (quotePublication != null && quotePublication is PublicationComment) {
                comment.quoteId = quotePublication.id
                comment.quoteText = quotePublication.creator.name + ": " + quotePublication.text
                comment.quoteCreatorName = quotePublication.creator.name
                if (quotePublication.imageIdArray.isNotEmpty()) comment.quoteImages = quotePublication.imageIdArray
                else if (quotePublication.imageId > 0) comment.quoteImages = Array(1) { quotePublication.imageId }
                else if (quotePublication.stickerId > 0) {
                    comment.quoteStickerId = quotePublication.stickerId
                    comment.quoteStickerImageId = quotePublication.stickerImageId
                } else comment.quoteImages = emptyArray()
            }
        }

        comment.fandom.id = publication!!.fandom.id
        comment.fandom.languageId = publication!!.fandom.languageId
        comment.category = publication!!.category
        comment.publicationType = API.PUBLICATION_TYPE_COMMENT
        comment.dateCreate = System.currentTimeMillis()
        comment.parentPublicationId = publicationId
        comment.status = API.STATUS_PUBLIC
        comment.parentCommentId = parentComment.id
        comment.parentPublicationType = publication!!.publicationType
        comment.answerName = parentComment.creator.name
        comment.creator = ControllerAccounts.instance(apiAccount.id,
                apiAccount.accessTag,
                apiAccount.accessTag,
                apiAccount.name,
                apiAccount.imageId,
                apiAccount.sex,
                apiAccount.accessTagSub)
        comment.jsonDB = comment.jsonDB(true, Json())

        ControllerPublications.put(comment)

        ControllerPublicationsHistory.put(comment.id, HistoryCreate(apiAccount.id, apiAccount.imageId, apiAccount.name))

        ControllerSubThread.inSub("EPublicationsCommentCreate") {
            if (watchPost) ControllerPublications.watchComments(apiAccount.id, publicationId, true)
            ControllerPublications.parseMentions(comment.text, comment.id, comment.publicationType, comment.parentPublicationId, comment.parentPublicationType, 0, apiAccount, arrayOf(if (parentComment != null) parentComment!!.creator.id else 0L, publication!!.creator.id))
            ControllerAccounts.updateCommentsCount(apiAccount.id, 1)
            Database.update("EPublicationsCommentCreate", SqlQueryUpdate(TPublications.NAME).where(TPublications.id, "=", publication!!.id).update(TPublications.subpublications_count, TPublications.subpublications_count + "+1"))
            notifications()
            achievements()
        }

        return Response(comment)
    }

    private fun achievements() {
        ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_COMMENTS_COUNT)

        if (parentComment.id == 0L) {
            ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_COMMENT)
            ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_COMMENT)
        } else {
            ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_ANSWER)
            ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_ANSWER)
        }

        ControllerQuests.addQuestProgress(apiAccount, API.QUEST_COMMENTS, 1)
    }

    private fun notifications() {

        if (parentComment.id != 0L && apiAccount.id != parentComment.creator.id && !ControllerCollisions.checkCollisionExist(parentComment.creator.id, apiAccount.id, API.COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT)) {
            val notification = NotificationCommentAnswer(apiAccount.imageId, publicationId, comment.id, apiAccount.id, apiAccount.sex, apiAccount.name, publication!!.publicationType, publication!!.tag_s_1, comment.text, comment.imageId, comment.imageIdArray, comment.stickerId, ControllerPublications.getMaskText(parentComment!!), ControllerPublications.getMaskPageType(parentComment!!))
            ControllerNotifications.push(parentComment.creator.id, notification)
        }


        val publicationName = ""

        val tokens = ControllerNotifications.getCommentWatchers(publicationId, apiAccount.id, parentComment.creator.id)

        val v = Database.select("ECommentsCreate.notifications", SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id)
            .where(SqlWhere.WhereIN(TCollisions.owner_id, tokens.map { it.a1 }.toTypedArray()))
            .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT)
            .where(TCollisions.collision_id, "=", apiAccount.id))
        val excludeBL: Array<Long> = Array(v.rowsCount) { v.next() }

        val notification = NotificationComment(apiAccount.imageId, publicationId, comment.id, apiAccount.id, apiAccount.sex, apiAccount.name, publication!!.publicationType, publication!!.creator.id, publication!!.tag_s_1, comment.text, comment.imageId, comment.imageIdArray, publication!!.fandom.name, publicationName, comment.stickerId, ControllerPublications.getMaskText(publication!!), ControllerPublications.getMaskPageType(publication!!))

        ControllerNotifications.push(notification, tokens.filterNot { excludeBL.contains(it.a1) }.toTypedArray())
    }

    private fun parseGif() {
        comment.type = PublicationComment.TYPE_GIF
        comment.gifId = ControllerResources.put(gif!!, API.RESOURCES_PUBLICATION_COMMENT)
        comment.imageId = ControllerResources.put(imageArray!![0], API.RESOURCES_PUBLICATION_COMMENT)
    }

    private fun parseImage() {
        if (imageArray!!.size == 1) {
            comment.type = PublicationComment.TYPE_IMAGE
            comment.imageId = ControllerResources.put(imageArray!![0], API.RESOURCES_PUBLICATION_COMMENT)
        } else {
            comment.type = PublicationComment.TYPE_IMAGES
            comment.imageIdArray = Array(imageArray!!.size) { ControllerResources.put(imageArray!![it], API.RESOURCES_PUBLICATION_COMMENT) }
        }
    }

    private fun parseSticker() {
        comment.type = PublicationComment.TYPE_STICKER
        comment.stickerId = stickerId
        comment.stickerImageId = sticker.imageId
        comment.stickerGifId = sticker.gifId
    }


}

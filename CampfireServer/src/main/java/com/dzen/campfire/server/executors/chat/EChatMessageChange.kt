package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessageChange
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.models.publications.history.HistoryEditPublic
import com.dzen.campfire.api.requests.chat.RChatMessageChange
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TChats
import com.sup.dev.java.libs.json.Json

class EChatMessageChange : RChatMessageChange(0, 0, "", false) {

    private var publication = PublicationChatMessage()

    override fun check() {
        text = ControllerCensor.cens(text)
        val publication = ControllerPublications.getPublication(publicationId, apiAccount.id) as PublicationChatMessage?
        if (publication == null) throw ApiException(API.ERROR_GONE)
        if (publication.creator.id != apiAccount.id) throw ApiException(API.ERROR_ACCESS) // see git blame
        if (publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_GONE)
        this.publication = publication
        if (text.length < API.CHAT_MESSAGE_TEXT_MIN_L || text.length > API.CHAT_MESSAGE_TEXT_MAX_L) throw ApiException(E_BAD_TEXT_SIZE)

        val tag = publication.chatTag()
        if (tag.chatType == API.CHAT_TYPE_FANDOM_SUB) {
            val chat = ControllerChats.getChat(tag.targetId, TChats.fandom_id, TChats.language_id)
            if (chat.isEmpty()) throw ApiException(API.ERROR_GONE)
            val fandomId: Long = chat.next()
            val languageId: Long = chat.next()
            ControllerAccounts.checkAccountBanned(apiAccount.id, fandomId, languageId)
        } else {
            ControllerAccounts.checkAccountBanned(apiAccount.id, publication.fandom.id, publication.fandom.languageId)
        }

        if (tag.chatType == API.CHAT_TYPE_CONFERENCE) {
            if(!ControllerChats.hasAccessToConf_Write(apiAccount.id, tag.targetId)) throw ApiException(API.ERROR_ACCESS)
        }
    }

    override fun execute(): Response {
        val oldText = publication.text
        publication.text = text
        publication.changed = true
        publication.quoteId = 0
        publication.quoteText = ""
        publication.quoteImagesIds = emptyArray()
        publication.quoteImagesPwd = emptyArray()
        publication.quoteCreatorName = ""
        publication.quoteStickerId = 0
        publication.quoteStickerImageId = 0
        publication.newFormatting = newFormatting

        if (quoteMessageId != 0L) {
            val quoteUnit = ControllerPublications.getPublication(quoteMessageId, apiAccount.id)
            if (quoteUnit != null && quoteUnit is PublicationChatMessage && quoteUnit.chatTag() == publication.chatTag()) {
                publication.quoteId = quoteUnit.id
                publication.quoteText = quoteUnit.creator.name + ": " + quoteUnit.text
                publication.quoteCreatorName = quoteUnit.creator.name
                publication.quoteImagesIds = if (quoteUnit.resourceId > 0) Array(1) { quoteUnit.resourceId } else quoteUnit.imageIdArray
                publication.quoteImagesPwd = if (quoteUnit.resourceId > 0) arrayOf(quoteUnit.imagePwd) else quoteUnit.imagePwdArray
                if (quoteUnit.stickerId > 0) {
                    publication.quoteStickerId = quoteUnit.stickerId
                    publication.quoteStickerImageId = quoteUnit.stickerImageId
                }
            }
        }

        ControllerPublications.replaceJson(publicationId, publication)

        val n = NotificationChatMessageChange(publication)
        ControllerNotifications.push(ControllerChats.getChatSubscribersIdsWithTokensNotDeleted(publication.chatTag(), apiAccount.id), n)
        ControllerNotifications.push(ControllerChats.getChatSubscribersIdsWithTokensNotSubscribed(publication.chatTag(), apiAccount.id), n)
        ControllerPublicationsHistory.put(publication.id, HistoryEditPublic(apiAccount.id, apiAccount.imageId, apiAccount.name, oldText))

        publication.jsonDB = publication.jsonDB(true, Json())
        return Response(publication)

    }

}

package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessageChange
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.models.publications.history.HistoryEditPublic
import com.dzen.campfire.api.requests.chat.RChatMessageChange
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.sup.dev.java.libs.json.Json

class EChatMessageChange : RChatMessageChange(0, 0, "") {

    private var publication = PublicationChatMessage()

    override fun check() {
        text = ControllerCensor.cens(text)
        val publication = ControllerPublications.getPublication(publicationId, apiAccount.id) as PublicationChatMessage?
        if (publication == null) throw ApiException(API.ERROR_GONE)
        if (publication.creator.id != apiAccount.id) throw ApiException(API.ERROR_ACCESS) // see git blame
        this.publication = publication
        if (text.length < API.CHAT_MESSAGE_TEXT_MIN_L || text.length > API.CHAT_MESSAGE_TEXT_MAX_L) throw ApiException(E_BAD_TEXT_SIZE)
        ControllerAccounts.checkAccountBanned(apiAccount.id, publication.fandom.id, publication.fandom.languageId)

        val tag = publication.chatTag()
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
        publication.quoteImages = emptyArray()

        if (quoteMessageId != 0L) {
            val quoteUnit = ControllerPublications.getPublication(quoteMessageId, apiAccount.id)
            if (quoteUnit != null && quoteUnit is PublicationChatMessage && quoteUnit.chatTag() == publication.chatTag()) {
                publication.quoteId = quoteUnit.id
                publication.quoteText = quoteUnit.creator.name + ": " + quoteUnit.text
                publication.quoteImages = if (quoteUnit.resourceId > 0) Array(1) { quoteUnit.resourceId } else quoteUnit.imageIdArray
                publication.quoteImagesPwd = if (quoteUnit.resourceId > 0) arrayOf(quoteUnit.imagePwd) else quoteUnit.imagePwdArray
            }
        }

        ControllerPublications.replaceJson(publicationId, publication)

        val n = NotificationChatMessageChange(publicationId, text)
        ControllerNotifications.push(n, ControllerChats.getChatSubscribersIdsWithTokensNotDeleted(publication.chatTag(), apiAccount.id))
        ControllerNotifications.push(n, ControllerChats.getChatSubscribersIdsWithTokensNotSubscribed(publication.chatTag(), apiAccount.id))
        ControllerPublicationsHistory.put(publication.id, HistoryEditPublic(apiAccount.id, apiAccount.imageId, apiAccount.name, oldText))

        publication.jsonDB = publication.jsonDB(true, Json())
        return Response(publication)

    }

}
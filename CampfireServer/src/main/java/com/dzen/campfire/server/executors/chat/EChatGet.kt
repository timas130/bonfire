package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.chat.RChatGet
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TChatsSubscriptions
import com.dzen.campfire.server.tables.TFandoms
import com.sup.dev.java_pc.sql.Database

class EChatGet : RChatGet(ChatTag(), 0) {

    private var isRetry = false

    override fun check() {

        if (messageId != 0L) {
            val publication = ControllerPublications.getPublication(messageId, apiAccount.id)
            if (publication == null) throw ApiException(API.ERROR_GONE)
            if (publication !is PublicationChatMessage) throw ApiException(API.ERROR_ACCESS)
            tag = publication.chatTag()
        }

        if (tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) {
            if (!API.isLanguageExsit(tag.targetSubId)) throw ApiException(API.ERROR_GONE)
            val v = ControllerFandom.get(tag.targetId, TFandoms.status)
            if (v.isEmpty()) throw ApiException(API.ERROR_GONE)
            if (v.next<Long>() != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        }
        tag.setMyAccountId(apiAccount.id)

    }

    override fun execute(): Response {

        val myChats = Database.select("EChatGet select_1",
                ControllerChats.instanceSelect_Subscriptions()
                        .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                        .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                        .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                        .where(TChatsSubscriptions.account_id, "=", apiAccount.id)
                        .offset_count(0, 1)
        )

        if (myChats.isEmpty && !isRetry) {
            if (tag.chatType == API.CHAT_TYPE_CONFERENCE) {
                if (!ControllerChats.getChatParams(tag.targetId).isPublic) {
                    throw ApiException(API.ERROR_ACCESS)
                }
            }

            ControllerChats.createSubscriptionIfNotExist(
                accountId = apiAccount.id,
                tag = tag,
                isSubscribeValue = 1,
                memberLvl = API.CHAT_MEMBER_LVL_USER,
                memberOwner = 0,
                enterDate = if (tag.chatType == API.CHAT_TYPE_PRIVATE) 0 else 1
            )
            isRetry = true
            return execute()
        }

        val chat = ControllerChats.parseSelect_Sunscriptions(apiAccount.id, myChats)[0]

        if (tag.chatType == API.CHAT_TYPE_CONFERENCE && chat.memberStatus == API.CHAT_MEMBER_STATUS_DELETE) throw ApiException(API.ERROR_ACCESS)


        return Response(chat)
    }

}

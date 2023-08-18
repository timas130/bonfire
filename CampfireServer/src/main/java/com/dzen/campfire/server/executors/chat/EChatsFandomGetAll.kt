package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.chat.Chat
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.chat.RChatsFandomGetAll
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerOptimizer
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TChats
import com.dzen.campfire.server.tables.TChatsSubscriptions
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere

class EChatsFandomGetAll : RChatsFandomGetAll(0, 0, 0) {

    override fun check() {

    }

    override fun execute(): Response {

        val list = ArrayList<Chat>()
        if (offset == 0L) list.add(getRoot())
        else offset - 1

        loadChats(list)

        return Response(list.toTypedArray())

    }

    private fun loadChats(list: ArrayList<Chat>) {
        val myChats = Database.select("EChatsFandomGetAll selectAll", ControllerChats.instanceSelect_Chat(apiAccount.id, anyAccountLastMessage = true)
                .where(TChats.fandom_id, "=", fandomId)
                .where(TChats.language_id, "=", languageId)
                .where(TChats.type, "=", API.CHAT_TYPE_FANDOM_SUB)
                .offset_count(offset, COUNT)
                .sort(TChats.date_create, true)
        )

        if (myChats.isEmpty) return

        val publicationsChat = ControllerChats.parseSelect_Chat(apiAccount.id, myChats)

        val messages = ControllerPublications.parseSelect(
                Database.select("EChatsGetAll select_2",
                        ControllerPublications.instanceSelect(apiAccount.id)
                                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                                .where(SqlWhere.WhereIN(TPublications.id, Array(publicationsChat.size) { publicationsChat[it].chatMessage.id }))
                )
        )

        for (c in publicationsChat) {
            for (i in messages) {
                if (i.id == c.chatMessage.id) {
                    c.chatMessage = i as PublicationChatMessage
                    break
                }
            }
            c.chatMessage.tag_1 = c.tag.chatType
            c.chatMessage.tag_2 = c.tag.targetId
            c.chatMessage.tag_3 = c.tag.targetSubId
        }

        list.addAll(publicationsChat)
    }

    private fun getRoot(): Chat {

        val tag = ChatTag(API.CHAT_TYPE_FANDOM_ROOT, fandomId, languageId)

        val chat = Chat()
        chat.chatMessage = ControllerChats.getLastMessage(tag) ?: PublicationChatMessage()
        chat.tag = tag

        if (chat.chatMessage.id == 0L) {
            val v = ControllerFandom.get(fandomId, TFandoms.name, TFandoms.image_id)

            chat.chatMessage.fandom.id = fandomId
            chat.chatMessage.fandom.languageId = languageId
            if (v.hasNext()) {
                chat.chatMessage.fandom.name = v.next()
                chat.chatMessage.fandom.imageId = v.next()
            }
        }

        chat.customName = chat.chatMessage.fandom.name
        chat.customImageId = chat.chatMessage.fandom.imageId
        chat.membersCount = ControllerOptimizer.getChatSubscribersCount(chat.tag)

        val chatInfoV = Database.select("EChatsFandomGetAll selectRoot",
                SqlQuerySelect(
                        TChatsSubscriptions.NAME,
                        TChatsSubscriptions.subscribed,
                        TChatsSubscriptions.new_messages
                )
                        .where(TChatsSubscriptions.account_id, "=", apiAccount.id)
                        .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                        .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                        .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
        )

        if (!chatInfoV.isEmpty) {
            val subscribed: Long = chatInfoV.next()
            chat.subscribed = subscribed > 0
            chat.unreadCount =  chatInfoV.next()
            if(chat.unreadCount < 0) chat.unreadCount = 0 // Костыль
        }

        return chat
    }

}
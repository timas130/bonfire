package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatParamsConf
import com.dzen.campfire.api.requests.chat.RChatGetForChange
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.server.tables.TChats
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.json.Json

class EChatGetForChange : RChatGetForChange(0) {

    private var myLvl = 0L

    override fun check() {
        val v = ControllerChats.getMemberLevelAndStatus(apiAccount.id, chatId)
        if (v == null || v.a2 != API.CHAT_MEMBER_STATUS_ACTIVE) throw ApiException(API.ERROR_ACCESS)
        myLvl = v.a1
    }

    override fun execute(): Response {

        val v = ControllerChats.getChat(chatId, TChats.name, TChats.image_id)
        val chatName: String = v.next()
        val chatImageId: Long = v.next()

        val accounts = ControllerChats.getMembers(chatId)

        val chatParams = ChatParamsConf(Json(ControllerChats.getChat(chatId, TChats.chat_params).next<String>()))

        return Response(chatName, chatImageId, myLvl, accounts, chatParams)
    }

}
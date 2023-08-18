package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.requests.chat.RChatEnter
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.api.tools.ApiException

class EChatEnter : RChatEnter(ChatTag()) {

    @Throws(ApiException::class)
    override fun check() {
        if(tag.chatType != API.CHAT_TYPE_CONFERENCE) throw ApiException(API.ERROR_ACCESS)
        if (ControllerChats.getMemberStatus(apiAccount.id, tag.targetId) != 2L) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        ControllerChats.enter(apiAccount, tag)
        ControllerChats.putEnter(apiAccount, tag)

        return Response()
    }
}

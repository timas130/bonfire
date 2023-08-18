package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.requests.chat.RChatRead
import com.dzen.campfire.server.controllers.ControllerChats

class EChatRead : RChatRead(ChatTag()) {

    override fun check() {
        tag.setMyAccountId(apiAccount.id)
    }

    override fun execute(): Response {
        val date = System.currentTimeMillis()
        ControllerChats.markRead(apiAccount.id, tag)

        return Response(date)
    }

}
package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.notifications.chat.NotificationChatTyping
import com.dzen.campfire.api.requests.chat.RChatTyping
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.server.controllers.ControllerNotifications

class EChatTyping : RChatTyping(ChatTag()) {

    override fun check() {
        tag.setMyAccountId(apiAccount.id)
    }

    override fun execute(): Response {

        if(tag.chatType == API.CHAT_TYPE_CONFERENCE){
            val memberStatus = ControllerChats.getMemberStatus(apiAccount.id, tag.targetId)
            if(memberStatus != API.CHAT_MEMBER_STATUS_ACTIVE)  return Response()
        }

        val n = NotificationChatTyping(tag, apiAccount.id, apiAccount.name, apiAccount.imageId)

        if (tag.chatType == API.CHAT_TYPE_PRIVATE) {
            ControllerNotifications.push(tag.getAnotherId(), n)
        } else  {
            ControllerNotifications.push(n, ControllerChats.getChatSubscribersIdsWithTokensNotDeleted(tag, apiAccount.id))
        }

        return Response()
    }
}
package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.requests.chat.RChatSubscribe
import com.dzen.campfire.server.controllers.ControllerAchievements
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.server.controllers.ControllerOptimizer
import com.dzen.campfire.api.tools.ApiException

class EChatSubscribe : RChatSubscribe(ChatTag(), false) {

    override fun check() {
        tag.setMyAccountId(apiAccount.id)
        if (tag.chatType != API.CHAT_TYPE_FANDOM_ROOT
                && tag.chatType != API.CHAT_TYPE_CONFERENCE) throw ApiException(E_BAD_CHAT_TYPE)
    }

    override fun execute(): Response {

        ControllerChats.subscribe(apiAccount.id, tag, subscribed)
        if(tag.chatType != API.CHAT_TYPE_CONFERENCE) ControllerChats.updateReadOrSubscribe(apiAccount.id, tag, 0L, API.CHAT_MEMBER_LVL_USER, 0, System.currentTimeMillis())

        if (subscribed) {
            ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_CHAT_SUBSCRIBE)
            ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_CHAT_SUBSCRIBE)
        }

        return Response()
    }
}
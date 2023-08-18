package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.requests.chat.RChatGetInfo
import com.dzen.campfire.server.controllers.ControllerCollisions

class EChatGetInfo : RChatGetInfo(ChatTag()) {

    override fun check() {
        tag.setMyAccountId(apiAccount.id)
    }

    override fun execute(): Response {

        var myAccountIsBlackList = false
        var anotherAccountIsBlackList = false

        if (tag.chatType == API.CHAT_TYPE_PRIVATE) {
            myAccountIsBlackList = ControllerCollisions.checkCollisionExist(tag.getAnotherId(), apiAccount.id, API.COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT)
            anotherAccountIsBlackList = ControllerCollisions.checkCollisionExist(apiAccount.id, tag.getAnotherId(), API.COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT)
        }

        return Response(
                myAccountIsBlackList,
                anotherAccountIsBlackList)
    }

}
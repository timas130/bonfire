package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.requests.chat.RChatLeave
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.server.tables.TChatsSubscriptions
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EChatLeave : RChatLeave(ChatTag()) {

    @Throws(ApiException::class)
    override fun check() {
        if(tag.chatType != API.CHAT_TYPE_CONFERENCE) throw ApiException(API.ERROR_ACCESS)
        if (!ControllerChats.hasAccessToConf_Edit(apiAccount.id, tag.targetId)) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        ControllerChats.putLeave(apiAccount, tag)

        Database.update("EChatLeave", SqlQueryUpdate(TChatsSubscriptions.NAME)
                .where(TChatsSubscriptions.account_id, "=", apiAccount.id)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                .update(TChatsSubscriptions.exit_date, System.currentTimeMillis())
                .update(TChatsSubscriptions.member_status, API.CHAT_MEMBER_STATUS_LEAVE)
        )

        return Response()
    }
}

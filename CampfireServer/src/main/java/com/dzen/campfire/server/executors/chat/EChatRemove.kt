package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.requests.chat.RChatRemove
import com.dzen.campfire.server.tables.TChatsSubscriptions
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EChatRemove : RChatRemove(ChatTag()) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {

        Database.update("EChatRemove", SqlQueryUpdate(TChatsSubscriptions.NAME)
                .where(TChatsSubscriptions.account_id, "=", apiAccount.id)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                .update(TChatsSubscriptions.subscribed, -1)
        )

        /*
                Database.update("EChatClearHistory", SqlQueryUpdate(TChatsSubscriptions.NAME)
                .where(TChatsSubscriptions.account_id, "=", apiAccount.id)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                .update(TChatsSubscriptions.enter_date, System.currentTimeMillis())
                .update(TChatsSubscriptions.last_message_id, 0)
                .update(TChatsSubscriptions.last_message_date, 0)
        )

         */

        return Response()
    }
}
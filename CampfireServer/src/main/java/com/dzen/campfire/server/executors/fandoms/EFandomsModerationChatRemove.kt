package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.moderations.chat.ModerationChatRemove
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationChatRemove
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerModeration
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TChats
import com.dzen.campfire.server.tables.TChatsSubscriptions
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EFandomsModerationChatRemove : RFandomsModerationChatRemove(0, "") {

    var fandomId = 0L
    var languageId = 0L
    var name = ""

    @Throws(ApiException::class)
    override fun check() {
        comment = ControllerModeration.parseComment(comment, apiAccount.id)

        val v = Database.select("EFandomsModerationChatRemove select", SqlQuerySelect(TChats.NAME, TChats.fandom_id, TChats.language_id, TChats.name)
                .where(TChats.id, "=", chatId)
        )

        if(v.isEmpty) throw ApiException(API.ERROR_GONE)

        fandomId = v.next()
        languageId = v.next()
        name = v.next()

        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_CHATS)

    }

    @Throws(ApiException::class)
    override fun execute(): Response {

        Database.remove("EFandomsModerationChatRemove remove_1", SqlQueryRemove(TChats.NAME)
                .whereValue(TChats.id, "=", chatId)
        )

        Database.remove("EFandomsModerationChatRemove remove_2", SqlQueryRemove(TChatsSubscriptions.NAME)
                .where(TChatsSubscriptions.chat_type, "=", API.CHAT_TYPE_FANDOM_SUB)
                .where(TChatsSubscriptions.target_id, "=", chatId)
        )

        ControllerPublications.moderation(ModerationChatRemove(comment, chatId, name), apiAccount.id, fandomId, languageId, 0)

        return Response()
    }
}

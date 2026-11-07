package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.models.publications.history.HistoryPublication
import com.dzen.campfire.api.requests.publications.RPublicationsHistoryGetAll
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TChatsSubscriptions
import com.dzen.campfire.server.tables.TPublicationsHistory
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EPublicationsHistoryGetAll : RPublicationsHistoryGetAll(0, 0) {

    override fun check() {
        val publication = ControllerPublications.getPublication(publicationId, apiAccount.id)
                ?: throw ApiException(API.ERROR_GONE)
        if (publication !is PublicationChatMessage) return

        val tag = ChatTag(publication.tag_1, publication.fandom.id, publication.fandom.languageId)
        when (tag.chatType) {
            API.CHAT_TYPE_PRIVATE -> {
                if (tag.targetId != apiAccount.id && tag.targetSubId != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
            }
            API.CHAT_TYPE_CONFERENCE -> {
                val v = ControllerChats.getSubscription(apiAccount.id, tag, TChatsSubscriptions.enter_date, TChatsSubscriptions.exit_date)
                val enterDate = v.nextLongOrZero()
                val exitDate = v.nextLongOrZero()
                if (enterDate == 0L || publication.dateCreate < enterDate || (exitDate > 0 && publication.dateCreate > exitDate))
                    throw ApiException(API.ERROR_ACCESS)
            }
        }
    }

    override fun execute(): Response {

        val v = Database.select("EPublicationsHistoryGetAll",SqlQuerySelect(TPublicationsHistory.NAME, TPublicationsHistory.id, TPublicationsHistory.data)
                .where(TPublicationsHistory.publication_id, "=", publicationId)
                .sort(TPublicationsHistory.date, false)
                .offset_count(offset, COUNT)
        )

        return Response(Array(v.rowsCount){
            val id :Long = v.next()
            val history = HistoryPublication()
            history.json(false, Json(v.next<String>()))
            history.id = id
            history
        })

    }
}


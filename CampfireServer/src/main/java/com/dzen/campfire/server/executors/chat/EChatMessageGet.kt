package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.chat.RChatMessageGet
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TChatsSubscriptions
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database

class EChatMessageGet : RChatMessageGet(ChatTag(), 0) {

    override fun check() {
        tag.setMyAccountId(apiAccount.id)
    }

    override fun execute(): Response {
        if (tag.chatType == API.CHAT_TYPE_CONFERENCE && getEnterDate() == 0L) throw ApiException(API.ERROR_ACCESS)

        val select = ControllerPublications.instanceSelect(apiAccount.id)
            .where(TPublications.id, "=", messageId)
            .where(TPublications.tag_1, "=", tag.chatType)
            .where(TPublications.fandom_id, "=", tag.targetId)
            .where(TPublications.language_id, "=", tag.targetSubId)
            .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_CHAT_MESSAGE)
        if (tag.chatType != API.CHAT_TYPE_FANDOM_ROOT) {
            select.where(TPublications.date_create, ">=", getEnterDate())
            if (getExitDate() > 0) select.where(TPublications.date_create, "<=", getExitDate())
        }

        val publications = ControllerPublications.parseSelect(Database.select("EChatMessageGet", select))

        if (publications.isEmpty()) throw ApiException(API.ERROR_GONE, GONE_REMOVE)

        val publication = publications[0] as PublicationChatMessage

        if (publication.status == API.STATUS_BLOCKED || publication.status == API.STATUS_DEEP_BLOCKED)
            throw ApiException.instance(API.ERROR_GONE, GONE_BLOCKED, ControllerPublications.getModerationIdForPublication(messageId))
        if (publication.status == API.STATUS_REMOVED) throw ApiException(API.ERROR_GONE, GONE_REMOVE)
        if (publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_GONE)

        ControllerPublications.loadSpecDataForPosts(apiAccount.id, arrayOf(publication))

        return Response(publication)
    }

    var enterDate:Long? = null
    var exitDate:Long? = null

    private fun getEnterDate():Long{
        if(enterDate == null)loadDates()
        return enterDate!!
    }
    private fun getExitDate():Long{
        if(exitDate == null)loadDates()
        return exitDate!!
    }
    private fun loadDates(){
        val vv = ControllerChats.getSubscription(apiAccount.id, tag, TChatsSubscriptions.enter_date, TChatsSubscriptions.exit_date)
        enterDate = vv.nextLongOrZero()
        exitDate = vv.nextLongOrZero()
    }
}
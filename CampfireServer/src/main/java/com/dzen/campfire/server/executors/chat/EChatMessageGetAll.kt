package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.chat.RChatMessageGetAll
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerSubThread
import com.dzen.campfire.server.tables.TChatsSubscriptions
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database

class EChatMessageGetAll : RChatMessageGetAll(ChatTag(), 0, false, 0) {

    override fun check() {
        tag.setMyAccountId(apiAccount.id)
    }

    override fun execute(): Response {

        if (tag.chatType == API.CHAT_TYPE_CONFERENCE && getEnterDate() == 0L) throw ApiException(API.ERROR_ACCESS)
        if (!old) ControllerSubThread.inSub("makeRead chat"){ControllerChats.markRead(apiAccount.id, tag)}

        val targetMessage = if (messageId > 0) ControllerPublications.getPublication(messageId, apiAccount.id) else null

        if (targetMessage == null) {
            val result = select(COUNT, old, offsetDate)
            return Response(result)
        }

        val select_1 = select(COUNT / 2, true, targetMessage.dateCreate)
        val select_2 = select(COUNT / 2, false, targetMessage.dateCreate)
        val list = ArrayList<PublicationChatMessage>()
        for (i in select_1) list.add(i)
        list.add(targetMessage as PublicationChatMessage)
        for (i in select_2) list.add(i)

        var array = list.map { it as Publication }.toTypedArray()
        ControllerPublications.loadBlacklists(apiAccount.id, array)

        @Suppress("UNCHECKED_CAST")
        return Response(array as Array<PublicationChatMessage>)
    }

    private fun select(count: Int, old: Boolean, offsetDate: Long): Array<PublicationChatMessage> {
        val select = ControllerPublications.instanceSelect(apiAccount.id)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_CHAT_MESSAGE)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.tag_1, "=", tag.chatType)
                .where(TPublications.fandom_id, "=", tag.targetId)
                .where(TPublications.language_id, "=", tag.targetSubId)

        if (tag.chatType != API.CHAT_TYPE_FANDOM_ROOT) {
            select.where(TPublications.date_create, ">=", getEnterDate())
            if (getExitDate() > 0) select.where(TPublications.date_create, "<=", getExitDate())
        }

        if (offsetDate == 0L) {
            select.where(TPublications.date_create, "<", java.lang.Long.MAX_VALUE)
            select.sort(TPublications.date_create, false)
        } else {
            select.where(TPublications.date_create, if (old) "<" else ">", offsetDate)
            select.sort(TPublications.date_create, !old)
        }

        select.offset_count(0, count)

        val v = Database.select("EChatMessageGetAll", select)

        var publications = ControllerPublications.parseSelect(v)
        if (old || offsetDate == 0L) publications.reverse()

        ControllerPublications.loadBlacklists(apiAccount.id, publications)
        publications = ControllerPublications.loadShadowBans(apiAccount.id, publications)

        return Array(publications.size) { publications[it] as PublicationChatMessage }
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

package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationBlocked
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.models.publications.moderations.PublicationModeration
import com.dzen.campfire.api.models.publications.moderations.publications.ModerationBlock
import com.dzen.campfire.api.requests.publications.RPublicationsBlockGetAll
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerPost.filterNsfw
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database

class EPublicationsBlockGetAll : RPublicationsBlockGetAll(0) {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_ADMIN)
    }

    override fun execute(): Response {

        val publications = ControllerPublications.parseSelect(Database.select("EPublicationsBlockGetAll",ControllerPublications.instanceSelect(apiAccount.id)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_MODERATION)
                .where(TPublications.tag_1, "=", API.MODERATION_TYPE_BLOCK)
                .where(TPublications.tag_2, "=", 0)
                .filterNsfw(apiAccount.id, requestApiVersion)
                .sort(TPublications.date_create, true)
                .offset_count(offset, COUNT)))

        return Response(Array(publications.size) {
            val u = PublicationBlocked()
            val e = (publications[it] as PublicationModeration).moderation as ModerationBlock
            u.moderationId = publications[it].id
            u.moderator =  publications[it].creator
            u.accountBlockDate = e.accountBlockDate
            u.lastPublicationsBlocked = e.lastPublicationsBlocked
            u.comment = e.comment

            val un = ControllerPublications.getPublication(e.publicationId, apiAccount.id)
            if(un == null){
                val m = PublicationChatMessage()
                m.text = "[null]"
                m.type = PublicationChatMessage.TYPE_TEXT
                m.publicationType = API.PUBLICATION_TYPE_CHAT_MESSAGE
                u.publication = m

            }else{
                u.publication = un
            }

            u
        })
    }
}

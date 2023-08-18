package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.publications.RPublicationsReactionGetAccounts
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlWhere

class EPublicationsReactionGetAccounts : RPublicationsReactionGetAccounts(0, 0) {

    override fun check() {

    }

    override fun execute(): Response {


        val publication = ControllerPublications.getPublication(publicationId, apiAccount.id) ?: throw ApiException(API.ERROR_GONE)

        if (publication !is PublicationComment && publication !is PublicationChatMessage) throw ApiException(API.ERROR_ACCESS)
        if (publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)

        val list = ArrayList<Long>()

        for(r in publication.reactions) if(r.reactionIndex == reactionIndex) list.add(r.accountId)

        val accounts = ControllerAccounts.parseSelect(Database.select("EPublicationsReactionGetAccounts", ControllerAccounts.instanceSelect()
                .where(SqlWhere.WhereIN(TAccounts.id,  list))))

        return Response(accounts)
    }


}

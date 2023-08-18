package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.publications.RPublicationsReactionRemove
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsCollections

class EPublicationsReactionRemove : RPublicationsReactionRemove(0, 0) {

    override fun check() {
    }

    override fun execute(): Response {

        val publication = ControllerPublications.getPublication(publicationId, apiAccount.id) ?: throw ApiException(API.ERROR_GONE)

        if (publication !is PublicationComment && publication !is PublicationChatMessage) throw ApiException(API.ERROR_ACCESS)
        if (publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)

        publication.reactions = ToolsCollections.removeIf(publication.reactions) { it.accountId == apiAccount.id && it.reactionIndex == reactionIndex }

        ControllerPublications.replaceJson(publication.id, publication)

        return Response()
    }


}

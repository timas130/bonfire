package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationReaction
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.publications.RPublicationsReactionAdd
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsCollections

class EPublicationsReactionAdd : RPublicationsReactionAdd(0, 0) {

    var publication:Publication = PublicationComment()

    override fun check() {
        publication = ControllerPublications.getPublication(publicationId, apiAccount.id) ?: throw ApiException(API.ERROR_GONE)
        ControllerAccounts.checkAccountBanned(apiAccount.id, publication.fandom.id, publication.fandom.languageId)
    }

    override fun execute(): Response {
        if (publication !is PublicationComment && publication !is PublicationChatMessage) throw ApiException(API.ERROR_ACCESS)
        if (publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)

        for (r in publication.reactions) if (r.accountId == apiAccount.id && r.reactionIndex == reactionIndex) throw ApiException(API.ERROR_ALREADY)

        publication.reactions = ToolsCollections.add(Publication.Reaction(apiAccount.id, reactionIndex), publication.reactions)
        ControllerPublications.replaceJson(publication.id, publication)

        if (apiAccount.id != publication.creator.id
                && !ControllerCollisions.checkCollisionExist(apiAccount.id, publication.id, API.COLLISION_PUBLICATION_REACTION)) {
            ControllerCollisions.putCollision(apiAccount.id, publication.id, API.COLLISION_PUBLICATION_REACTION)
            val notification = NotificationPublicationReaction(apiAccount.imageId, publication.id, publication.publicationType, publication.parentPublicationId, reactionIndex, apiAccount.id, apiAccount.sex, publication.parentPublicationType, apiAccount.name, publication.tag_s_1, ControllerPublications.getMaskText(publication), ControllerPublications.getMaskPageType(publication))
            ControllerNotifications.push(publication.creator.id, notification)
        }

        return Response()
    }


}

package com.dzen.campfire.server.executors.comments

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.requests.comments.RCommentGet
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database

class ECommentGet : RCommentGet(0, 0) {

    override fun check() {

    }

    override fun execute(): Response {
        val publications = ControllerPublications.parseSelect(Database.select("ECommentGet",
                ControllerPublications.instanceSelect(apiAccount.id)
                        .where(TPublications.id, "=", commentId)
                        .where(TPublications.parent_publication_id, "=", parentPublicationId)
                        .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_COMMENT)
        ))

        if (publications.isEmpty()) throw ApiException(API.ERROR_GONE, GONE_REMOVE)

        val publication = publications[0] as PublicationComment

        if (publication.status == API.STATUS_BLOCKED || publication.status == API.STATUS_DEEP_BLOCKED)
            throw ApiException.instance(API.ERROR_GONE, GONE_BLOCKED, ControllerPublications.getModerationIdForPublication(commentId))
        if (publication.status == API.STATUS_REMOVED) throw ApiException(API.ERROR_GONE, GONE_REMOVE)
        if (publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_GONE)

        ControllerPublications.loadSpecDataForPosts(apiAccount.id, arrayOf(publication))

        return Response(publication)
    }

}
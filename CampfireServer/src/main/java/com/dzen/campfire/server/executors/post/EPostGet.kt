package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostGet
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.rust.RustProfile
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database

class EPostGet : RPostGet(0) {

    override fun check() {

    }

    override fun execute(): Response {
        val publications = ControllerPublications.parseSelect(Database.select("EPostGet",
                ControllerPublications.instanceSelect(apiAccount.id)
                        .where(TPublications.id, "=", publicationId)
                        .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)
        ))

        if (publications.isEmpty()) throw ApiException(API.ERROR_GONE, GONE_REMOVE)

        val publication = publications[0] as PublicationPost

        if ((publication.status == API.STATUS_BLOCKED || publication.status == API.STATUS_DEEP_BLOCKED) && !ControllerFandom.can(apiAccount, API.LVL_PROTOADMIN))
            throw ApiException.instance(API.ERROR_GONE, GONE_BLOCKED, ControllerPublications.getModerationIdForPublication(publicationId))
        if (publication.status == API.STATUS_REMOVED && !ControllerFandom.can(apiAccount, API.LVL_PROTOADMIN)) throw ApiException(API.ERROR_GONE, GONE_REMOVE)
        if (publication.status != API.STATUS_PUBLIC) {
            if ((publication.status != API.STATUS_PENDING && publication.status != API.STATUS_DRAFT) || apiAccount.id != publication.creator.id) {
                throw ApiException(API.ERROR_GONE)
            }
        }

        val canSeeNsfw = RustProfile.canSeeNsfw(apiAccount.id)
        if (canSeeNsfw == false) throw ApiException(API.ERROR_ACCESS)

        ControllerPublications.loadSpecDataForPosts(apiAccount.id, arrayOf(publication))

        return Response(publication, ControllerPublications.getTags(apiAccount.id, publication.id))
    }
}

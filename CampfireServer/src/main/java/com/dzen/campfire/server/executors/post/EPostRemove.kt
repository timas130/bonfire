package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostRemove
import com.dzen.campfire.server.controllers.ControllerPost
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.json.Json

class EPostRemove : RPostRemove(0) {

    private var publication: PublicationPost? = null

    @Throws(ApiException::class)
    override fun check() {
        val v = ControllerPublications[publicationId, TPublications.status, TPublications.creator_id, TPublications.publication_json, TPublications.publication_type]

        if (v.isEmpty()) throw ApiException(API.ERROR_GONE)

        val status = v.next<Long>()
        val creatorId = v.next<Long>()
        publication = PublicationPost(Json(v.next<Object>().toString()))
        val publicationType = v.next<Long>()

        if (status != API.STATUS_DRAFT && status != API.STATUS_PUBLIC) throw ApiException(E_BAD_STATUS)
        if (publicationType != API.PUBLICATION_TYPE_POST) throw ApiException(E_BAD_TYPE)
        if (creatorId != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        ControllerPost.remove(publication!!)

        return Response()
    }
}

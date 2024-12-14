package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryEditPublic
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostRemovePage
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerPublicationsHistory
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsMapper

class EPostRemovePage : RPostRemovePage(0,  emptyArray()) {

    private var publication: PublicationPost = PublicationPost()

    @Throws(ApiException::class)
    override fun check() {
        val v = ControllerPublications[publicationId, TPublications.status, TPublications.creator_id, TPublications.publication_json, TPublications.publication_type]

        if (v.isEmpty()) throw ApiException(API.ERROR_GONE)

        val status = v.next<Long>()
        val creatorId = v.next<Long>()
        publication = PublicationPost(Json(v.next<Object>().toString()))
        val publicationType = v.next<Long>()

        if (status != API.STATUS_DRAFT && status != API.STATUS_PUBLIC && status != API.STATUS_PENDING) throw ApiException(E_BAD_STATUS)
        if (creatorId != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
        if (publicationType != API.PUBLICATION_TYPE_POST) throw ApiException(E_BAD_TYPE)
        for (index in pageIndexes) if (index < 0 || publication.pages.size <= index) throw ApiException(E_BAD_PAGE_INDEX, "index [" + index + "] size[" + publication.pages.size + "]")
        if (publication.status == API.STATUS_PUBLIC && publication.pages.size == 1) throw ApiException(E_CANT_REMOVE_LAST)
    }

    override fun execute(): Response {
        for (index in pageIndexes) remove(index)

        return Response()
    }

    private fun remove(pageIndex: Int) {
        val pages = publication.pages
        publication.pages = ToolsMapper.subarray(publication.pages, 0, publication.pages.size - 1)
        var x = 0
        for (i in pages.indices)
            if (i != pageIndex) publication.pages[x++] = pages[i]

        if(publication.status == API.STATUS_PUBLIC){
            ControllerPublicationsHistory.put(publicationId, HistoryEditPublic(apiAccount.id, apiAccount.imageId, apiAccount.name))
        }

        if (publication.pages.isEmpty())
            ControllerPublications.remove(publicationId)
        else
            ControllerPublications.replaceJson(publicationId, publication)

    }

}

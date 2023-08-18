package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryEditPublic
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostMovePage
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerPublicationsHistory
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsMapper
import java.util.*

class EPostMovePage : RPostMovePage(0, 0, 0) {

    private var publication: PublicationPost? = null

    @Throws(ApiException::class)
    override fun check() {
        val v = ControllerPublications[publicationId, TPublications.status, TPublications.creator_id, TPublications.publication_type, TPublications.publication_json]

        if (v.isEmpty()) throw ApiException(API.ERROR_GONE)

        val status = v.next<Long>()
        val creatorId = v.next<Long>()
        val publicationType = v.next<Long>()
        publication = PublicationPost(Json(v.next<Object>().toString()))

        if (status != API.STATUS_DRAFT && status != API.STATUS_PUBLIC && status != API.STATUS_PENDING) throw ApiException(E_BAD_STATUS)
        if (creatorId != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
        if (publicationType != API.PUBLICATION_TYPE_POST) throw ApiException(E_BAD_TYPE)
        if (pageIndex < 0 || publication!!.pages.size <= pageIndex) throw ApiException(E_BAD_PAGE_INDEX, "index [" + pageIndex + "] size[" + publication!!.pages.size + "]")
        if (targetIndex < 0 || publication!!.pages.size <= targetIndex) throw ApiException(E_BAD_PAGE_INDEX, "index [" + targetIndex + "] size[" + publication!!.pages.size + "]")

        if (status == API.STATUS_PUBLIC) ControllerAccounts.checkAccountBanned(apiAccount.id, publication!!.fandom.id, publication!!.fandom.languageId)
    }

    override fun execute(): Response {

        val list = ArrayList(listOf(*publication!!.pages))
        list.add(targetIndex, list.removeAt(pageIndex))

        val array = arrayOfNulls<Page>(list.size)
        for (i in list.indices) array[i] = list[i]

        publication!!.pages = ToolsMapper.asNonNull(array)

        ControllerPublications.replaceJson(publicationId, publication!!)

        if(publication!!.status == API.STATUS_PUBLIC){
            ControllerPublicationsHistory.put(publicationId, HistoryEditPublic(apiAccount.id, apiAccount.imageId, apiAccount.name))
        }

        return Response()
    }


}

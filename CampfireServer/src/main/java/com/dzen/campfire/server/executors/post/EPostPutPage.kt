package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.history.HistoryCreate
import com.dzen.campfire.api.models.publications.history.HistoryEditPublic
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostPublication
import com.dzen.campfire.api.requests.post.RPostPutPage
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database

class EPostPutPage : RPostPutPage(0, emptyArray(), 0, 0, "", "") {

    private var fandom: Fandom? = null
    private var publication: PublicationPost? = null

    @Throws(ApiException::class)
    override fun check() {

        //  Убрать в начале Августа
        if(fandomId == 1L){
            if(appKey == "RegGuides:1") fandomId = 1924
            if(appKey == "RegGuides:2") fandomId = 1925
            if(appKey == "RegGuides:3") fandomId = 1926
            if(appKey == "RegGuides:4") fandomId = 1927
        }
        //  -----------------------


        if (publicationId != 0L) {
            val v = ControllerPublications[publicationId, TPublications.status, TPublications.publication_type, TPublications.publication_json, TPublications.creator_id, TPublications.fandom_id, TPublications.language_id]

            if (v.isEmpty()) throw ApiException(API.ERROR_GONE)

            val status = v.next<Long>()
            val publicationType = v.next<Long>()
            publication = PublicationPost(Json(v.next<Object>().toString()))
            publication!!.id = publicationId
            val creatorId = v.next<Long>()
            val fandomId = v.next<Long>()
            val languageId = v.next<Long>()

            if (status != API.STATUS_PUBLIC && status != API.STATUS_DRAFT && status != API.STATUS_PENDING) throw ApiException(E_BAD_STATUS)
            if (publicationType != API.PUBLICATION_TYPE_POST) throw ApiException(E_BAD_TYPE)
            if (status == API.STATUS_PUBLIC) ControllerAccounts.checkAccountBanned(apiAccount.id, fandomId, languageId)
            if (creatorId != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
            if (pages.isEmpty() && publication!!.pages.size >= API.POST_MAX_PAGES_COUNT) throw ApiException(E_BAD_PAGES_COUNT)
            if (pages.isNotEmpty() && publication!!.pages.size + pages.size > API.POST_MAX_PAGES_COUNT) throw ApiException(E_BAD_PAGES_COUNT)
        } else {
            fandom = ControllerFandom.getFandom(fandomId)
            if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(RPostPublication.E_FANDOM_NOT_PUBLIC)
        }

        for (p in pages) ControllerPost.checkPage(p, E_BAD_PAGE, false, API.PAGES_SOURCE_TYPE_POST)
    }

    override fun execute(): Response {

        for (p in pages) insertPage(p)

        return Response(publication!!.id, pages)
    }

    private fun insertPage(page: Page) {


        if (publication == null) {
            publication = PublicationPost()
            publication!!.id = Database.insert("EPostPutPage insertPage",TPublications.NAME,
                    TPublications.publication_type, API.PUBLICATION_TYPE_POST,
                    TPublications.fandom_id, fandomId,
                    TPublications.language_id, languageId,
                    TPublications.date_create, System.currentTimeMillis(),
                    TPublications.creator_id, apiAccount.id,
                    TPublications.publication_json, publication!!.jsonDB(true, Json()).toString(),
                    TPublications.parent_publication_id, 0,
                    TPublications.parent_fandom_closed, ControllerFandom.get(fandomId, TFandoms.fandom_closed).next<Int>(),
                    TPublications.tag_s_1, appKey,
                    TPublications.tag_s_2, appSubKey,
                    TPublications.publication_category, ControllerFandom.getCategory(fandomId),
                    TPublications.status, API.STATUS_DRAFT,
                    TPublications.fandom_key, "$fandomId-$languageId-0"
            )

            ControllerPublicationsHistory.put(publication!!.id, HistoryCreate(apiAccount.id, apiAccount.imageId, apiAccount.name))
        }

        ControllerPost.insertPage(page, publication!!.id)

        val pages = publication!!.pages
        publication!!.pages = arrayOf(*pages, page)

        ControllerPublications.replaceJson(publication!!.id, publication!!)

        if(publication!!.status == API.STATUS_PUBLIC){
            ControllerPublicationsHistory.put(publicationId, HistoryEditPublic(apiAccount.id, apiAccount.imageId, apiAccount.name))
        }

    }

}

package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryEditPublic
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PageLinkImage
import com.dzen.campfire.api.models.publications.post.PageVideo
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostChangePage
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.rust.RustVideo
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.json.Json

class EPostChangePage : RPostChangePage(0, null, 0) {

    private var publication: PublicationPost? = null

    @Throws(ApiException::class)
    override fun check() {
        val v = ControllerPublications[publicationId, TPublications.status, TPublications.creator_id, TPublications.publication_type, TPublications.publication_json, TPublications.status, TPublications.fandom_id, TPublications.language_id]

        if (v.isEmpty()) throw ApiException(API.ERROR_GONE)

        val status:Long = v.next()
        val creatorId:Long = v.next()
        val publicationType:Long = v.next()
        publication = PublicationPost(Json(v.next<Object>().toString()))
        publication!!.id = publicationId
        publication!!.status = v.next()
        val fandomId:Long = v.next()
        val languageId:Long = v.next()

        if (status != API.STATUS_DRAFT && status != API.STATUS_PUBLIC&& status != API.STATUS_PENDING) throw ApiException(E_BAD_STATUS)
        if (creatorId != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
        if (publicationType != API.PUBLICATION_TYPE_POST) throw ApiException(E_BAD_TYPE)
        if (pageIndex < 0 || publication!!.pages.size <= pageIndex) throw ApiException(E_BAD_PAGE_INDEX, "index [" + pageIndex + "] size[" + publication!!.pages.size + "]")

        if (status == API.STATUS_PUBLIC) ControllerAccounts.checkAccountBanned(apiAccount.id, fandomId, languageId)
    }

    override fun execute(): Response {

        val resultPage: Page

        val oldVideoId = (publication!!.pages[pageIndex] as? PageVideo)?.videoId

        if (publication!!.pages[pageIndex].isRemoveOnChange()) {
            ControllerPost.stripClientResourceIds(page!!)
            page!!.copyChangeData(publication!!.pages[pageIndex])
            ControllerPost.checkPage(page, E_BAD_PAGE, true, API.PAGES_SOURCE_TYPE_POST, apiAccount.id, publicationId)
            val newLinkImage = page as? PageLinkImage
            val keepsOldImage = publication!!.pages[pageIndex] is PageLinkImage && newLinkImage != null && newLinkImage.insertBytes == null
            if (!keepsOldImage) ControllerPost.removePage(publication!!.pages[pageIndex])
            resultPage = page!!
        } else {
            publication!!.pages[pageIndex].prepareForServer(page!!)
            ControllerPost.checkPage(publication!!.pages[pageIndex], E_BAD_PAGE, true, API.PAGES_SOURCE_TYPE_POST, apiAccount.id, publicationId)
            resultPage = publication!!.pages[pageIndex]
        }

        // the same video can't be attached to two pages of one post
        if (resultPage is PageVideo) {
            for (i in publication!!.pages.indices) {
                val p = publication!!.pages[i]
                if (i != pageIndex && p is PageVideo && p.videoId == resultPage.videoId) throw ApiException(E_BAD_PAGE, "Duplicate videoId")
            }
        }

        if(publication!!.status == API.STATUS_PUBLIC) {
            ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_CHANGE_PUBLICATION)
            ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_CHANGE_PUBLICATION)
            ControllerPublicationsHistory.put(publicationId, HistoryEditPublic(apiAccount.id, apiAccount.imageId, apiAccount.name))
        }

        ControllerPost.insertPage(resultPage, publication!!.id)
        publication!!.pages[pageIndex] = resultPage
        ControllerPublications.replaceJson(publicationId, publication!!)

        // release the old video only after the new one is claimed and the page is persisted
        val newVideoId = (resultPage as? PageVideo)?.videoId
        if (oldVideoId != null && oldVideoId != newVideoId) RustVideo.release(oldVideoId)

        return Response(resultPage)

    }


}

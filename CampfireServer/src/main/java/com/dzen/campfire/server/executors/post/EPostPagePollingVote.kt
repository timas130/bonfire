package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PagePolling
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostPagePollingVote
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TAccounts

class EPostPagePollingVote : RPostPagePollingVote(0, 0, 0, 0, 0) {
    companion object {
        fun getPolling(sourceType: Long, sourceId: Long, sourceIdSub: Long, pollingId: Long): PagePolling? {
            var pages: Array<Page> = emptyArray()

            if (sourceType == API.PAGES_SOURCE_TYPE_POST || sourceType == 0L /*Обратная совместимость*/) {
                val publication = ControllerPublications.getPublication(sourceId, 0)
                if (publication == null || publication !is PublicationPost) throw ApiException(API.ERROR_GONE)
                pages = publication.pages
            }
            if (sourceType == API.PAGES_SOURCE_TYPE_WIKI) {
                val wikiPages =
                    ControllerWiki.getPagesByItemId(sourceId, sourceIdSub) ?: throw ApiException(API.ERROR_GONE)
                pages = wikiPages.pages
            }

            for (p in pages) {
                if (p is PagePolling && p.pollingId == pollingId) {
                    return p
                }
            }
            return null
        }
    }

    @Throws(ApiException::class)
    override fun check() {
        val polling = getPolling(sourceType, sourceId, sourceIdSub, pollingId)

        if (polling == null) throw ApiException(API.ERROR_GONE)
        if (polling.minLevel > apiAccount.accessTag) throw ApiException(E_LOW_LEVEL)
        if (polling.minKarma > apiAccount.accessTagSub) throw ApiException(E_LOW_KARMA)
        val days = (System.currentTimeMillis() - apiAccount.dateCreate) / (3600000L * 24) + 1
        if (polling.minDays > days) throw ApiException(E_LOW_DAYS)

        if (polling.blacklist.find { it.id == apiAccount.id } != null)
            throw ApiException(E_BLACKLISTED)
    }

    @Throws(ApiException::class)
    override fun execute(): Response {

        if (ControllerCollisions.checkCollisionExist(apiAccount.id, pollingId, API.COLLISION_PAGE_POLLING_VOTE))
            throw ApiException(E_ALREADY)
        ControllerCollisions.putCollisionValue1(apiAccount.id, pollingId, itemId, API.COLLISION_PAGE_POLLING_VOTE, System.currentTimeMillis())

        return Response()
    }
}

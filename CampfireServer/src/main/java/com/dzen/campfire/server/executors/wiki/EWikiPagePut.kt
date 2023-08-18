package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.wiki.WikiPages
import com.dzen.campfire.api.requests.wiki.RWikiPagePut
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerPost
import com.dzen.campfire.server.controllers.ControllerWiki
import com.dzen.campfire.api.tools.ApiException

class EWikiPagePut : RWikiPagePut(0, emptyArray(), 0) {

    private var wikiPagesOld: WikiPages? = null

    @Throws(ApiException::class)
    override fun check() {

        val wikiTitle = ControllerWiki.getTitlesByItemId(wikiItemId)
        if (wikiTitle == null) throw ApiException(API.ERROR_GONE)
        if (wikiTitle.wikiStatus != API.STATUS_PUBLIC) throw ApiException(E_BAD_STATUS)
        if (wikiTitle.itemType != API.WIKI_TYPE_ARTICLE) throw ApiException(E_BAD_TYPE)

        ControllerFandom.checkCan(apiAccount, wikiTitle.fandomId, languageId, API.LVL_MODERATOR_WIKI_EDIT)

        wikiPagesOld = ControllerWiki.getPagesByItemId_OnlyPublic(wikiItemId, languageId)

        if (wikiPagesOld != null) {
            if (pages.isEmpty() && wikiPagesOld!!.pages.size >= API.POST_MAX_PAGES_COUNT) throw ApiException(E_BAD_PAGES_COUNT)
            if (pages.isNotEmpty() && wikiPagesOld!!.pages.size + pages.size > API.POST_MAX_PAGES_COUNT) throw ApiException(E_BAD_PAGES_COUNT)
        }

        val fandom = ControllerFandom.getFandom(wikiTitle.fandomId)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)

        for (p in pages) ControllerPost.checkPage(p, E_BAD_PAGE, false)
    }

    override fun execute(): Response {

        for (p in pages) insertPage(p)

        return Response(pages)
    }

    private fun insertPage(page: Page) {

        ControllerPost.insertPage(page, API.RESOURCES_PUBLICATION_WIKI)

        val wikiPagesNew = WikiPages()
        wikiPagesNew.itemId = wikiItemId
        wikiPagesNew.languageId = languageId
        wikiPagesNew.creatorId = apiAccount.id
        wikiPagesNew.changeDate = System.currentTimeMillis()
        wikiPagesNew.wikiStatus = API.STATUS_PUBLIC
        wikiPagesNew.eventType = API.WIKI_PAGES_EVENT_TYPE_PUT

        if(wikiPagesOld != null) wikiPagesNew.pages = arrayOf(*wikiPagesOld!!.pages, page)
        else wikiPagesNew.pages = arrayOf(page)

        ControllerWiki.insertPages(wikiPagesNew)

    }

}

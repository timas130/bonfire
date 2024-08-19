package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.wiki.WikiPages
import com.dzen.campfire.api.requests.wiki.RWikiPageChange
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EWikiPageChange : RWikiPageChange(0, 0, null, 0) {

    private var wikiPagesOld = WikiPages()

    @Throws(ApiException::class)
    override fun check() {

        val wikiTitle = ControllerWiki.getTitlesByItemId(wikiItemId)
        if (wikiTitle == null) throw ApiException(API.ERROR_GONE)
        if (wikiTitle.wikiStatus != API.STATUS_PUBLIC) throw ApiException(E_BAD_STATUS)
        if (wikiTitle.itemType != API.WIKI_TYPE_ARTICLE) throw ApiException(E_BAD_TYPE)

        ControllerFandom.checkCan(apiAccount, wikiTitle.fandomId, languageId, API.LVL_MODERATOR_WIKI_EDIT)

        ControllerFandom.checkCan(apiAccount, wikiTitle.fandomId, languageId, API.LVL_MODERATOR_WIKI_EDIT)
        val wikiPagesOldX = ControllerWiki.getPagesByItemId_OnlyPublic(wikiItemId, languageId)
        if (wikiPagesOldX == null) throw ApiException(API.ERROR_GONE)
        wikiPagesOld = wikiPagesOldX

        val fandom = ControllerFandom.getFandom(wikiTitle.fandomId)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)

        if (pageIndex < 0 || wikiPagesOld.pages.size <= pageIndex) throw ApiException(E_BAD_PAGE_INDEX, "index [" + pageIndex + "] size[" + wikiPagesOld.pages.size + "]")

    }

    override fun execute(): Response {

        val resultPage: Page

        if (wikiPagesOld.pages[pageIndex].isRemoveOnChange()) {
            ControllerPost.checkPage(page, E_BAD_PAGE, true, API.PAGES_SOURCE_TYPE_WIKI)
            ControllerPost.removePage(wikiPagesOld.pages[pageIndex])
            resultPage = page!!
        } else {
            wikiPagesOld.pages[pageIndex].prepareForServer(page!!)
            ControllerPost.checkPage(wikiPagesOld.pages[pageIndex], E_BAD_PAGE, true, API.PAGES_SOURCE_TYPE_WIKI)
            resultPage = wikiPagesOld.pages[pageIndex]
        }

        ControllerPost.insertPage(resultPage, API.RESOURCES_PUBLICATION_WIKI)

        val wikiPagesNew = WikiPages()
        wikiPagesNew.itemId = wikiItemId
        wikiPagesNew.languageId = languageId
        wikiPagesNew.creatorId = apiAccount.id
        wikiPagesNew.changeDate = System.currentTimeMillis()
        wikiPagesNew.wikiStatus = API.STATUS_PUBLIC
        wikiPagesNew.eventType = API.WIKI_PAGES_EVENT_TYPE_CHANGE

        wikiPagesNew.pages = wikiPagesOld.pages
        wikiPagesNew.pages[pageIndex] = resultPage

        ControllerWiki.insertPages(wikiPagesNew)

        return Response(resultPage)

    }


}

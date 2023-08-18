package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiPages
import com.dzen.campfire.api.requests.wiki.RWikiPageRemove
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerWiki
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsMapper

class EWikiPageRemove : RWikiPageRemove(0, 0, emptyArray()) {

    private var wikiPagesOld = WikiPages()

    @Throws(ApiException::class)
    override fun check() {

        val wikiTitle = ControllerWiki.getTitlesByItemId(wikiItemId)
        if (wikiTitle == null) throw ApiException(API.ERROR_GONE)
        if (wikiTitle.wikiStatus != API.STATUS_PUBLIC) throw ApiException(E_BAD_STATUS)
        if (wikiTitle.itemType != API.WIKI_TYPE_ARTICLE) throw ApiException(E_BAD_TYPE)

        ControllerFandom.checkCan(apiAccount, wikiTitle.fandomId, languageId, API.LVL_MODERATOR_WIKI_EDIT)
        val wikiPagesOldX = ControllerWiki.getPagesByItemId_OnlyPublic(wikiItemId, languageId)
        if (wikiPagesOldX == null) throw ApiException(API.ERROR_GONE)
        wikiPagesOld = wikiPagesOldX

        val fandom = ControllerFandom.getFandom(wikiTitle.fandomId)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)

        for (index in pageIndexes) if (index < 0 || wikiPagesOld.pages.size <= index) throw ApiException(E_BAD_PAGE_INDEX, "index [" + index + "] size[" + wikiPagesOld.pages.size + "]")
    }

    override fun execute(): Response {
        for (index in pageIndexes) remove(index)

        return Response()
    }

    private fun remove(pageIndex: Int) {

        val wikiPagesNew = WikiPages()
        wikiPagesNew.itemId = wikiItemId
        wikiPagesNew.languageId = languageId
        wikiPagesNew.creatorId = apiAccount.id
        wikiPagesNew.changeDate = System.currentTimeMillis()
        wikiPagesNew.wikiStatus = API.STATUS_PUBLIC
        wikiPagesNew.eventType = API.WIKI_PAGES_EVENT_TYPE_REMOVE

        wikiPagesNew.pages = ToolsMapper.subarray(wikiPagesOld.pages, 0, wikiPagesOld.pages.size - 1)
        var x = 0
        for (i in wikiPagesNew.pages.indices) if (i != pageIndex) wikiPagesNew.pages[x++] = wikiPagesOld.pages[i]

        ControllerWiki.insertPages(wikiPagesNew)
    }

}

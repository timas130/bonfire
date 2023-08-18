package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiPages
import com.dzen.campfire.api.requests.wiki.RWikiItemHistoryCancel
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerWiki

class EWikiItemHistoryCancel : RWikiItemHistoryCancel(0) {

    var pages = WikiPages()

    override fun check() {

        val pages = ControllerWiki.getPagesById(pagesId) ?: throw ApiException(API.ERROR_GONE)
        this.pages = pages
        val title = ControllerWiki.getTitlesByItemId(pages.itemId) ?: throw ApiException(API.ERROR_GONE)

        ControllerFandom.checkCan(apiAccount, title.fandomId, pages.languageId, API.LVL_MODERATOR_WIKI_EDIT)
    }


    override fun execute(): Response {
        val newId = ControllerWiki.cancelPages(pages)
        if (newId < 1) throw ApiException(E_LAST_ITEM)

        return Response(newId)
    }


}
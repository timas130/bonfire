package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiArticleChangeLanguage
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerWiki
import com.dzen.campfire.api.tools.ApiException

class EWikiArticleChangeLanguage : RWikiArticleChangeLanguage(0, 0, 0) {

    var item = WikiTitle()

    override fun check() {
        val itemX = ControllerWiki.getTitlesByItemId(itemId)
        if (itemX == null) throw ApiException(API.ERROR_GONE)
        item = itemX
        ControllerFandom.checkCan(apiAccount, item.fandomId, fromLanguageId, API.LVL_MODERATOR_WIKI_EDIT)
        ControllerFandom.checkCan(apiAccount, item.fandomId, toLanguageId, API.LVL_MODERATOR_WIKI_EDIT)
    }


    override fun execute(): Response {
        val wikiPagesFrom = ControllerWiki.getPagesByItemId_OnlyPublic(itemId, fromLanguageId)

        if (wikiPagesFrom == null) throw ApiException(API.ERROR_GONE)

        val pages = wikiPagesFrom.pages

        wikiPagesFrom.eventType = API.WIKI_PAGES_EVENT_TYPE_PUT

        wikiPagesFrom.languageId = fromLanguageId
        wikiPagesFrom.pages = emptyArray()
        ControllerWiki.insertPages(wikiPagesFrom)

        wikiPagesFrom.languageId = toLanguageId
        wikiPagesFrom.pages = pages
        ControllerWiki.insertPages(wikiPagesFrom)

        return Response(wikiPagesFrom)
    }


}
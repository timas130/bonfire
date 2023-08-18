package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.requests.wiki.RWikiGetPages
import com.dzen.campfire.server.controllers.ControllerWiki

class EWikiGetPages : RWikiGetPages(0, 0) {

    override fun check() {

    }

    override fun execute(): Response {

        val wikiPages = ControllerWiki.getPagesByItemId_OnlyPublic(itemId, languageId)

        return Response(wikiPages)
    }


}
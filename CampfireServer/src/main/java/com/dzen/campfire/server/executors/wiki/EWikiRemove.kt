package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiRemove
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerWiki
import com.dzen.campfire.api.tools.ApiException

class EWikiRemove : RWikiRemove(0) {

    var item = WikiTitle()

    override fun check() {
        val itemX = ControllerWiki.getTitlesByItemId(itemId)
        if (itemX == null) throw ApiException(API.ERROR_GONE)
        item = itemX
        ControllerFandom.checkCan(apiAccount, item.fandomId, 1, API.LVL_MODERATOR_WIKI_EDIT)
    }


    override fun execute(): Response {
        ControllerWiki.markAsRemoved(itemId)
        return Response()
    }


}
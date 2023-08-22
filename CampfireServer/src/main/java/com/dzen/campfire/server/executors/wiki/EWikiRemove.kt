package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiRemove
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerWiki

class EWikiRemove : RWikiRemove(0) {

    var item = WikiTitle()

    override fun check() {
        val itemX = ControllerWiki.getTitlesByItemId(itemId) ?: throw ApiException(API.ERROR_GONE)
        item = itemX
        if (!API.LANGUAGES.any { ControllerFandom.can(apiAccount, item.fandomId, it.id, API.LVL_MODERATOR_WIKI_EDIT) }) {
            throw ApiException(API.ERROR_ACCESS)
        }
    }


    override fun execute(): Response {
        ControllerWiki.markAsRemoved(itemId)
        return Response()
    }


}

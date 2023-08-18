package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiItemMove
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerWiki
import com.dzen.campfire.server.tables.TWikiItems
import com.dzen.campfire.server.tables.TWikiTitles
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EWikiItemMove : RWikiItemMove(0, 0, 0) {
    private lateinit var wikiTitleItem: WikiTitle

    override fun check() {
        wikiTitleItem = ControllerWiki.getTitlesByItemId(itemId) ?: throw ApiException(API.ERROR_GONE)
        if (wikiTitleItem.wikiStatus != API.STATUS_PUBLIC) throw ApiException(E_BAD_STATUS)

        if (destinationId > 0) {
            val wikiTitleDest = ControllerWiki.getTitlesByItemId(destinationId)
                    ?: throw ApiException(API.ERROR_GONE)
            if (wikiTitleDest.wikiStatus != API.STATUS_PUBLIC) throw ApiException(E_BAD_STATUS)
            if (wikiTitleDest.itemType != API.WIKI_TYPE_SECION) throw ApiException(E_BAD_PAGE_INDEX)

            if (wikiTitleItem.fandomId != wikiTitleDest.fandomId) throw ApiException(E_BAD_PAGE_INDEX)
            if (destinationId == itemId) throw ApiException(E_BAD_PAGE_INDEX) // мне лень
        }

        ControllerFandom.checkCan(apiAccount, wikiTitleItem.fandomId, 1, API.LVL_MODERATOR_WIKI_EDIT)
    }

    override fun execute(): Response {
        wikiTitleItem.parentItemId = destinationId
        val j = Json()
        wikiTitleItem.json(true, j)

        Database.update("RWikiItemMove_move_title", SqlQueryUpdate(TWikiTitles.NAME)
                .where(TWikiTitles.item_id, "=", itemId)
                .update(TWikiTitles.parent_item_id, destinationId)
                .updateValue(TWikiTitles.item_data, j.toString()))
        Database.update("RWikiItemMove_move_item", SqlQueryUpdate(TWikiItems.NAME)
                .where(TWikiItems.id, "=", itemId)
                .update(TWikiItems.parent_item_id, destinationId))

        return Response()
    }
}
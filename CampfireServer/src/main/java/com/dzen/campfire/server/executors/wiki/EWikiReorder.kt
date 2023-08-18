package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiReorder
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerWiki
import com.dzen.campfire.server.tables.TWikiTitles
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EWikiReorder : RWikiReorder(0, 0) {
    private lateinit var wikiTitle: WikiTitle
    private lateinit var wikiTitleBefore: WikiTitle

    override fun check() {
        if (itemId == beforeId) throw ApiException(E_BAD_PAGE_INDEX)

        wikiTitle = ControllerWiki.getTitlesByItemId(itemId) ?: throw ApiException(API.ERROR_GONE)
        if (wikiTitle.wikiStatus != API.STATUS_PUBLIC) throw ApiException(E_BAD_STATUS)

        wikiTitleBefore = ControllerWiki.getTitlesByItemId(itemId) ?: throw ApiException(API.ERROR_GONE)
        if (wikiTitleBefore.wikiStatus != API.STATUS_PUBLIC) throw ApiException(E_BAD_STATUS)

        if (
                wikiTitle.fandomId != wikiTitleBefore.fandomId ||
                wikiTitle.parentItemId != wikiTitleBefore.parentItemId
        ) throw ApiException(E_BAD_PAGE_INDEX)

        ControllerFandom.checkCan(apiAccount, wikiTitle.fandomId, 1, API.LVL_MODERATOR_WIKI_EDIT)
    }

    override fun execute(): Response {
        val select = SqlQuerySelect(TWikiTitles.NAME, TWikiTitles.item_id)
                .where(TWikiTitles.ITEM_STATUS, "=", API.STATUS_PUBLIC)
                .where(TWikiTitles.wiki_status, "=", API.STATUS_PUBLIC)
                .sort(TWikiTitles.priority, true)

        if (wikiTitle.parentItemId > 0) {
            select.where(TWikiTitles.parent_item_id, "=", wikiTitle.parentItemId)
        } else {
            select.where(TWikiTitles.parent_item_id, "=", 0)
            select.where(TWikiTitles.fandom_id, "=", wikiTitle.fandomId)
        }

        val v = Database.select("EWikiReorder_select", select)

        // we have to update the whole section because by default priority is 0
        var idx = 1
        var tgtPriority = 0
        while (v.hasNext()) {
            val id: Long = v.next()
            val priority = if (id == beforeId) {
                idx += 2
                tgtPriority = idx - 2
                idx - 1
            } else {
                idx++
            }
            Database.update("EWikiReorder_update", SqlQueryUpdate(TWikiTitles.NAME)
                    .where(TWikiTitles.item_id, "=", id)
                    .update(TWikiTitles.priority, priority))
        }

        println(tgtPriority)
        Database.update("EWikiReorder_finish", SqlQueryUpdate(TWikiTitles.NAME)
                .where(TWikiTitles.item_id, "=", itemId)
                .update(TWikiTitles.priority, tgtPriority))

        return Response()
    }
}
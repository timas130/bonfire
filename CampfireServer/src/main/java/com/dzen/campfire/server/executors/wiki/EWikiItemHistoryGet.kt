package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.wiki.RWikiItemHistoryGet
import com.dzen.campfire.server.controllers.ControllerWiki
import com.dzen.campfire.server.tables.TWikiPages
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlWhere

class EWikiItemHistoryGet : RWikiItemHistoryGet(0, 0, 0) {

    override fun check() {

    }


    override fun execute(): Response {
        val select = ControllerWiki.instanceSelectPages()
                .where(SqlWhere.WhereIN(TWikiPages.wiki_status, arrayOf(API.STATUS_ARCHIVE, API.STATUS_PUBLIC, API.STATUS_REMOVED)))
                .where(TWikiPages.item_id, "=", itemId)
                .where(TWikiPages.language_id, "=", languageId)
                .sort(TWikiPages.date_create, false)
                .offset_count(offset, COUNT)

        val v = ControllerWiki.parseSelectPages(Database.select("EWikiItemHistoryGet",select))

        return Response(v)
    }


}
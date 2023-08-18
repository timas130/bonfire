package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.wiki.RWikiGet
import com.dzen.campfire.server.controllers.ControllerWiki
import com.dzen.campfire.server.tables.TWikiTitles
import com.sup.dev.java_pc.sql.Database

class EWikiGet : RWikiGet(0) {

    override fun check() {

    }

    override fun execute(): Response {
        val v = Database.select("EWikiGet", ControllerWiki.instanceSelectTitles()
                .where(TWikiTitles.item_id, "=", itemId)
                .where(TWikiTitles.ITEM_STATUS, "=", API.STATUS_PUBLIC))

        return Response(ControllerWiki.parseSelectTitles(v)[0])
    }


}
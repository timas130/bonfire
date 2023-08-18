package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.wiki.RWikiItemGet
import com.dzen.campfire.api.requests.wiki.RWikiListGet
import com.dzen.campfire.server.controllers.ControllerWiki
import com.dzen.campfire.server.tables.TWikiTitles
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database

class EWikiItemGet : RWikiItemGet(0) {

    override fun check() {

    }


    override fun execute(): Response {
        val select = ControllerWiki.instanceSelectTitles()
                .where(TWikiTitles.ITEM_STATUS, "=", API.STATUS_PUBLIC)
                .where(TWikiTitles.item_id, "=", itemId)

        val v =ControllerWiki.parseSelectTitles( Database.select("EWikiItemGet",select))

        if(v.isEmpty()) throw ApiException(API.ERROR_GONE)

        return Response(v[0])
    }


}
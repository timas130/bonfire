package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.wiki.RWikiListGet
import com.dzen.campfire.server.controllers.ControllerWiki
import com.dzen.campfire.server.tables.TWikiTitles
import com.sup.dev.java_pc.sql.Database

class EWikiListGet : RWikiListGet(0, 0, 0) {

    override fun check() {

    }


    override fun execute(): Response {
        val select = ControllerWiki.instanceSelectTitles()
                .where(TWikiTitles.ITEM_STATUS, "=", API.STATUS_PUBLIC)
                .where(TWikiTitles.wiki_status, "=", API.STATUS_PUBLIC)
                .sort(TWikiTitles.priority, true)
                .offset(offset)
                .count(COUNT)

        if(parentItemId > 0){
            select.where(TWikiTitles.parent_item_id, "=", parentItemId)
        }else{
            select.where(TWikiTitles.parent_item_id, "=", 0)
            select.where(TWikiTitles.fandom_id, "=", fandomId)
        }

        val v = Database.select("EWikiListGet",select)

        val array = ControllerWiki.parseSelectTitles(v)

        return Response(array)
    }


}
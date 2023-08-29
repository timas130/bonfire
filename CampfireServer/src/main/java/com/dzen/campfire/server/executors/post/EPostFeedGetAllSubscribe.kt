package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.post.RPostFeedGetAllSubscribe
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlWhere

class EPostFeedGetAllSubscribe : RPostFeedGetAllSubscribe(0, 0) {

    override fun check() {
    }

    override fun execute(): Response {
        val select = ControllerPublications.instanceSelect(apiAccount.id)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.date_create, "<", if (offsetDate == 0L) Long.MAX_VALUE else offsetDate)
                .count(COUNT)
                .sort(TPublications.date_create, false)

        if (categoryId > 0) select.where(TPublications.publication_category, "=", categoryId)

        val subscribeTag: String = ControllerAccounts.get(apiAccount.id, TAccounts.subscribes).next()!!
        val split = subscribeTag.split(',')
        var subscribeTagFandoms = "'-1'"
        for (i in split) {
            val xxx = i.split("-")
            if (xxx.size > 1) subscribeTagFandoms += ",${xxx[0]}'"
        }

        if (subscribeTag.isEmpty()) return Response(emptyArray())
        select.where(SqlWhere.WhereString(
                "(${TPublications.fandom_key} IN ($subscribeTag)" +
                        " OR (${TPublications.language_id}=-1 AND ${TPublications.fandom_id} IN($subscribeTagFandoms)))"
        ))

        var posts = ControllerPublications.parseSelect(Database.select("EPostFeedGetAll", select))
        posts = ControllerPublications.loadSpecDataForPosts(apiAccount.id, posts)

        return Response(posts)
    }

}

package com.dzen.campfire.server.executors.quests

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.post.RPostFeedGetAll
import com.dzen.campfire.api.requests.quests.RQuestsGetLatest
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlWhere

class EQuestsGetLatest : RQuestsGetLatest(0, emptyArray()) {
    override fun execute(): Response {
        val select = ControllerPublications.instanceSelect(apiAccount.id)
            .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_QUEST)
            .where(SqlWhere.WhereIN(TPublications.language_id, arrayOf(*languageIds, -1L)))
            .where(TPublications.status, "=", API.STATUS_PUBLIC)
            .where(TPublications.date_create, "<", if (offsetDate == 0L) Long.MAX_VALUE else offsetDate)
            .count(RPostFeedGetAll.COUNT)
            .sort(TPublications.date_create, false)

        val quests = ControllerPublications.parseSelect(Database.select("EQuestsGetLatest", select))

        return Response(quests)
    }
}
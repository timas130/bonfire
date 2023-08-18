package com.dzen.campfire.server.executors.quests

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.requests.quests.RQuestsGetDrafts
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database

class EQuestsGetDrafts : RQuestsGetDrafts(0) {
    override fun execute(): Response {
        val select = ControllerPublications.instanceSelect(0)
            .where(TPublications.creator_id, "=", apiAccount.id)
            .where(TPublications.status, "=", API.STATUS_DRAFT)
            .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_QUEST)
            .offset_count(offset, COUNT)
        val v = ControllerPublications.parseSelect(
            Database.select("EQuestsGetDrafts", select)
        ).map { a -> a as QuestDetails }.toTypedArray()

        return Response(v)
    }
}

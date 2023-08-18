package com.dzen.campfire.server.executors.quests

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.requests.quests.RQuestsPublish
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerUserQuests
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EQuestsPublish : RQuestsPublish(0) {
    override fun check() {
        ControllerAccounts.checkAccountBanned(apiAccount.id)
        ControllerFandom.checkCan(apiAccount, API.LVL_CREATE_QUESTS)
        checkQuestEditable(questId, apiAccount)

        val req = EQuestsGetParts()
        req.isDraft = true
        req.id = questId
        val parts = req.execute().parts // problem?
        val details = ControllerPublications.getPublication(questId, apiAccount.id)!! as QuestDetails

        if (parts.isEmpty()) throw ApiException(BAD_PART, "quest is empty")
        if (parts[0].type != API.QUEST_PART_TYPE_TEXT) throw ApiException(BAD_PART, "first part must be text")
        for (part in parts) {
            val partOk = ControllerUserQuests.checkPart(details, part, parts)
            if (!partOk)
                throw ApiException(BAD_PART, "one part failed a check")
        }

        if (details.description.isBlank())
            throw ApiException(NO_DESCRIPTION, "you must write a description for the quest")

        // "ok" to publish
    }

    override fun execute(): Response {
        Database.update("EQuestsPublish", SqlQueryUpdate(TPublications.NAME)
            .update(TPublications.status, API.STATUS_PUBLIC)
            .update(TPublications.date_create, System.currentTimeMillis())
            .where(TPublications.id, "=", questId))

        return Response()
    }
}
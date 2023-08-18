package com.dzen.campfire.server.executors.quests

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.requests.quests.RQuestsSaveState
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TQuestParts
import com.dzen.campfire.server.tables.TQuestStates
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EQuestsSaveState : RQuestsSaveState(0, Json(), 0) {
    private lateinit var questDetails: QuestDetails

    override fun check() {
        questDetails = ControllerPublications.getPublication(questId, apiAccount.id) as QuestDetails
        if (questDetails.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_GONE)

        val totalParts = Database.select("EQuestsSaveState count", SqlQuerySelect(TQuestParts.NAME, Sql.COUNT)
            .where(TQuestParts.unit_id, "=", questId)).nextLongOrZero()

        if (stateIndex >= totalParts || stateIndex < 0) throw ApiException(BAD_STATE, "stateIndex >= totalParts")

        stateVariables.forEach { key, value ->
            if (questDetails.variablesMap!![key.toLong()] == null) {
                throw ApiException(BAD_STATE, "variable not found")
            }

            if (value !is String)
                throw ApiException(BAD_STATE, "value !is String")
            if (value.length > API.QUEST_VARIABLE_MAX_VALUE_L)
                throw ApiException(BAD_STATE, "value.length > API.QUEST_VARIABLE_MAX_VALUE_L")
        }
    }

    override fun execute(): Response {
        val existingId = Database.select("EQuestsSaveState existing", SqlQuerySelect(TQuestStates.NAME, TQuestStates.id)
            .where(TQuestStates.user_id, "=", apiAccount.id)
            .where(TQuestStates.unit_id, "=", questId)
            .count(1))
            .nextLongOrZero()

        val json = Json()
        json.put("stateVariables", stateVariables)
        json.put("stateIndex", stateIndex)

        if (existingId > 0) {
            Database.update("EQuestsSaveState update", SqlQueryUpdate(TQuestStates.NAME)
                .updateValue(TQuestStates.json_db, json.toString()))
        } else {
            Database.insert(
                "EQuestsSaveState insert",
                TQuestStates.NAME,
                TQuestStates.unit_id, questId,
                TQuestStates.user_id, apiAccount.id,
                TQuestStates.json_db, json.toString(),
            )
        }

        return Response()
    }
}

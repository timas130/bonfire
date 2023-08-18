package com.dzen.campfire.server.executors.quests

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.quests.QuestPart
import com.dzen.campfire.api.requests.quests.RQuestsGetParts
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.server.tables.TQuestParts
import com.dzen.campfire.server.tables.TQuestStates
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import kotlin.properties.Delegates

class EQuestsGetParts : RQuestsGetParts(0) {
    var isDraft by Delegates.notNull<Boolean>()

    override fun check() {
        val v = Database.select("EQuestsGetParts check", SqlQuerySelect(
            TPublications.NAME, TPublications.status, TPublications.creator_id
        ).where(TPublications.id, "=", id))
        val status = v.next<Long>()
        val creator = v.next<Long>()
        if (status != API.STATUS_PUBLIC && (creator != apiAccount.id && status != API.STATUS_DRAFT))
            throw ApiException(API.ERROR_ACCESS, "not public")
        isDraft = status == API.STATUS_DRAFT
    }

    override fun execute(): Response {
        val parts = Database.select(
            "EQuestsGetParts execute",
            SqlQuerySelect(TQuestParts.NAME, TQuestParts.id, TQuestParts.json_db)
                .where(TQuestParts.unit_id, "=", id)
                .sort(TQuestParts.part_order, true)
        )
        val ret = arrayListOf<QuestPart>()
        while (parts.hasNext()) {
            val id = parts.next<Long>()
            val json = Json(parts.next<String>())
            val part = QuestPart.instance(json)
            part.id = id
            ret.add(part)
        }

        var stateVariables = Json()
        var stateIndex = 0
        if (!isDraft) {
            val state = Database.select("EQuestsGetParts state", SqlQuerySelect(TQuestStates.NAME, TQuestStates.json_db)
                .where(TQuestStates.unit_id, "=", id)
                .where(TQuestStates.user_id, "=", apiAccount.id)
                .count(1))
            val json = Json(if (!state.isEmpty) state.next() else "{}")
            stateVariables = json.getJson("stateVariables", Json())!!
            stateIndex = json.getInt("stateIndex", 0)
        }

        return Response(ret.toTypedArray(), stateVariables, stateIndex)
    }
}

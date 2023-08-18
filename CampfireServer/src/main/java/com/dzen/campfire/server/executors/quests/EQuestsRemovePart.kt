package com.dzen.campfire.server.executors.quests

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.quests.QuestPart
import com.dzen.campfire.api.requests.quests.RQuestsRemovePart
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerUserQuests
import com.dzen.campfire.server.tables.TQuestParts
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EQuestsRemovePart : RQuestsRemovePart(0, 0) {
    override fun check() {
        checkQuestEditable(questId, apiAccount)
        ControllerAccounts.checkAccountBanned(apiAccount.id)
    }

    override fun execute(): Response {
        val v = Database.select("EQuestsRemovePart_clean", SqlQuerySelect(TQuestParts.NAME, TQuestParts.json_db)
            .where(TQuestParts.unit_id, "=", questId)
            .where(TQuestParts.id, "=", partId))
        if (v.isEmpty) throw ApiException(API.ERROR_GONE)
        val part = QuestPart.instance(Json(v.next<String>()))

        ControllerUserQuests.partClean(part)
        Database.remove("EQuestsRemovePart_remove", SqlQueryRemove(TQuestParts.NAME)
            .where(TQuestParts.unit_id, "=", questId)
            .where(TQuestParts.id, "=", partId))

        return Response()
    }
}
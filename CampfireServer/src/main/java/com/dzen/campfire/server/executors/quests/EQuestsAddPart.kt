package com.dzen.campfire.server.executors.quests

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.requests.quests.RQuestsAddPart
import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerUserQuests
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.server.tables.TQuestParts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect

fun checkQuestEditable(questId: Long, apiAccount: ApiAccount) {
    val v = ControllerPublications[questId, TPublications.status, TPublications.creator_id, TPublications.publication_type]

    if (v.isEmpty()) throw ApiException(API.ERROR_ACCESS)

    val status = v.next<Long>()
    val creator = v.next<Long>()
    val type = v.next<Long>()

    if (status != API.STATUS_DRAFT) throw ApiException(API.ERROR_ACCESS)
    if (creator != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
    if (type != API.PUBLICATION_TYPE_QUEST) throw ApiException(API.ERROR_ACCESS)
}

class EQuestsAddPart : RQuestsAddPart(0, emptyArray()) {
    override fun check() {
        ControllerAccounts.checkAccountBanned(apiAccount.id)
        checkQuestEditable(questId, apiAccount)

        val partCount = Database.select(
            "EQuestsAddPart 1", SqlQuerySelect(TQuestParts.NAME, Sql.COUNT)
                .where(TQuestParts.unit_id, "=", questId)
        ).next<Long>() + parts.size
        if (partCount > API.QUEST_PARTS_MAX) throw ApiException(TOO_MANY_PARTS)

        val details = ControllerPublications.getPublication(questId, apiAccount.id)!! as QuestDetails

        for ((idx, part) in parts.withIndex()) {
            if (!ControllerUserQuests.checkPart(details, part))
                throw ApiException(BAD_PART, "part $idx failed the check", arrayOf(idx.toString()))
        }
    }

    override fun execute(): Response {
        var order = Database.select(
            "EQuestsAddPart 2", SqlQuerySelect(TQuestParts.NAME, Sql.MAX(TQuestParts.part_order))
                .where(TQuestParts.unit_id, "=", questId),
        ).nextMayNullOrNull<Int>()?.toLong() ?: 0

        for (part in parts) {
            part.id = ControllerUserQuests.insertPart(++order, questId, part)
        }

        return Response(parts)
    }
}
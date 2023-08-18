package com.dzen.campfire.server.executors.quests

import com.dzen.campfire.api.requests.quests.RQuestsReorderPart
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TQuestParts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EQuestsReorderPart : RQuestsReorderPart(0, 0, 0) {
    override fun check() {
        checkQuestEditable(questId, apiAccount)
        ControllerAccounts.checkAccountBanned(apiAccount.id)
    }

    override fun execute(): Response {
        // todo: convert all of this info a postgres function?
        //       if the quest is too big, it might take too much time.
        // also it's basically stolen from EWikiReorder, so it's terrible by definition

        val v = Database.select("EQuestsReorderPart_select", SqlQuerySelect(TQuestParts.NAME, TQuestParts.id, TQuestParts.part_order)
            .where(TQuestParts.unit_id, "=", questId)
            .sort(TQuestParts.part_order, true))

        var idx = 1L
        var tgtOrder = 0L
        while (v.hasNext()) {
            val id: Long = v.next()
            val sourceOrder: Long = v.next()
            val order = if (id == partIdBefore) {
                tgtOrder = idx
                idx += 2
                idx - 1
            } else {
                idx++
            }
            if (sourceOrder == order) continue
            Database.update("EQuestsReorderPart_reorder", SqlQueryUpdate(TQuestParts.NAME)
                .where(TQuestParts.id, "=", id)
                .update(TQuestParts.part_order, order))
        }

        println("moving $partId from x to $tgtOrder")
        Database.update("EQuestsReorderPart_finish", SqlQueryUpdate(TQuestParts.NAME)
            .where(TQuestParts.id, "=", partId)
            .update(TQuestParts.part_order, tgtOrder))
        return Response()
    }
}
package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.chat.RChatGetSubscribers
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TChatsSubscriptions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere

class EChatGetSubscribers : RChatGetSubscribers(0, 0, 0) {

    override fun check() {

    }

    override fun execute(): Response {


        val v = Database.select("EChatGetSubscribers select_1", SqlQuerySelect(TChatsSubscriptions.NAME, TChatsSubscriptions.account_id)
                .where(TChatsSubscriptions.chat_type, "=", API.CHAT_TYPE_FANDOM_ROOT)
                .where(TChatsSubscriptions.target_id, "=", fandomId)
                .where(TChatsSubscriptions.target_sub_id, "=", languageId)
                .where(TChatsSubscriptions.subscribed, "=", 1)
                .where(TChatsSubscriptions.read_date,  ">", System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 2)
                .offset_count(offset, COUNT)
        )

        if (v.isEmpty) return Response(emptyArray())

        val accountsIds = Array<Long>(v.rowsCount){ v.next() }

        val accounts = ControllerAccounts.parseSelect(Database.select("EChatMessageGetAll select_2", ControllerAccounts.instanceSelect()
                .where(SqlWhere.WhereIN(TAccounts.id, accountsIds))))

        return Response(accounts)
    }

}
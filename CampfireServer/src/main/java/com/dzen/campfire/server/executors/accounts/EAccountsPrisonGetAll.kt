package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.models.account.AccountPrison
import com.dzen.campfire.api.requests.accounts.RAccountsPrisonGetAll
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database

class EAccountsPrisonGetAll : RAccountsPrisonGetAll(0) {

    override fun check() {
    }

    override fun execute(): Response {

        val v = Database.select("EAccountsPrisonGetAll", ControllerAccounts.instanceSelect()
                .addColumn(TAccounts.ban_date)
                .where(TAccounts.ban_date, ">", System.currentTimeMillis())
                .sort(TAccounts.ban_date, true)
                .offset_count(offset, COUNT))

        val prisions = Array(v.rowsCount) {
            val prison = AccountPrison()
            prison.account = ControllerAccounts.parseSelectOne(v)
            prison.banDate = v.next()
            prison
        }

        return Response(prisions)
    }

}

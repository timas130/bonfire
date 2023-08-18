package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.account.AccountReports
import com.dzen.campfire.api.requests.accounts.RAccountsReportsGetAll
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerOptimizer
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EAccountsReportsGetAll : RAccountsReportsGetAll(0) {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_BAN)
    }

    override fun execute(): Response {

        val v = Database.select("EAccountsReportsGetAll", SqlQuerySelect(TAccounts.NAME,
                TAccounts.id,
                TAccounts.lvl,
                TAccounts.last_online_time,
                TAccounts.name,
                TAccounts.img_id,
                TAccounts.sex,
                TAccounts.karma_count_30,
                TAccounts.reports_count)
                .where(TAccounts.reports_count, ">", 0)
                .sort(TAccounts.reports_count, false)
                .offset_count(offset, COUNT))

        return Response(Array(v.rowsCount){
            val acR = AccountReports()
            acR.account = ControllerAccounts.instance(v)
            acR.reportsCount = v.next()
            acR
        })
    }

}

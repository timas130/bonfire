package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsGetAllOnline
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database

class EAccountsGetAllOnline : RAccountsGetAllOnline(0) {

    override fun check() {

    }

    override fun execute(): Response {

        return Response(ControllerAccounts.getOnlineByDate(offsetDate, COUNT))
    }

}
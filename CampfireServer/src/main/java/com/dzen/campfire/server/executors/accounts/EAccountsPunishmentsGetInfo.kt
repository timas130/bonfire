package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsPunishmentsGetInfo
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts

class EAccountsPunishmentsGetInfo : RAccountsPunishmentsGetInfo(0) {

    override fun check() {

    }

    override fun execute(): Response {

        val v = ControllerAccounts.get(accountId, TAccounts.BANS_COUNT, TAccounts.WARNS_COUNT)

        return Response(v.next(), v.next())
    }


}

package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsGetIgnoredFandoms
import com.dzen.campfire.server.controllers.ControllerAccounts

class EAccountsGetIgnoredFandoms : RAccountsGetIgnoredFandoms(0) {

    override fun check() {

    }

    override fun execute(): Response {
        return Response(ControllerAccounts.getBlackListFandoms(accountId))
    }


}

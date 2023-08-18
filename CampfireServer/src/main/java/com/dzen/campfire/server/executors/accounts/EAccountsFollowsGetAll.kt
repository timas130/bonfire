package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsFollowsGetAll
import com.dzen.campfire.server.controllers.ControllerAccounts

class EAccountsFollowsGetAll : RAccountsFollowsGetAll(0, 0, false) {

    override fun check() {

    }

    override fun execute(): Response {

        val accounts = if (followers) ControllerAccounts.getFollowers(followsOfaAccountId, offset, COUNT) else ControllerAccounts.getFollows(followsOfaAccountId, offset, COUNT)

        return Response(accounts)
    }


}
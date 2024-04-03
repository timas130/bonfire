package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListRemove
import com.dzen.campfire.server.controllers.ControllerCollisions

class EAccountsBlackListRemove : RAccountsBlackListRemove(0) {
    override fun check() {

    }

    override fun execute(): Response {
        ControllerCollisions.removeCollisions(apiAccount.id, accountId, API.COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT)

        return Response()
    }
}

package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListAdd
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerCollisions

class EAccountsBlackListAdd : RAccountsBlackListAdd(0) {
    override fun check() {
        if (apiAccount.id == accountId) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {
        ControllerCollisions.putCollisionWithCheck(apiAccount.id, accountId, API.COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT)

        return Response()
    }
}

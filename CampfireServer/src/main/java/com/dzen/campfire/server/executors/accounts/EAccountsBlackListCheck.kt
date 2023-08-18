package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListCheck
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerCollisions

class EAccountsBlackListCheck : RAccountsBlackListCheck(0) {

    override fun check() {
        if(apiAccount.id == accountId) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {
        return Response(ControllerCollisions.checkCollisionExist(apiAccount.id, accountId, API.COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT))
    }


}

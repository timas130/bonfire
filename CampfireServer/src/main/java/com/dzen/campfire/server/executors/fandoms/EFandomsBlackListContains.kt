package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListAdd
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListAdd
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListContains
import com.dzen.campfire.server.controllers.ControllerCollisions

class EFandomsBlackListContains : RFandomsBlackListContains(0) {

    override fun check() {

    }

    override fun execute(): Response {
        return Response(ControllerCollisions.checkCollisionExist(apiAccount.id, fandomId, API.COLLISION_ACCOUNT_BLACK_LIST_FANDOM))
    }


}

package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListRemove
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListRemove
import com.dzen.campfire.server.controllers.ControllerCollisions

class EFandomsBlackListRemove : RFandomsBlackListRemove(0) {

    override fun check() {

    }

    override fun execute(): Response {

        ControllerCollisions.removeCollisions(apiAccount.id, fandomId, API.COLLISION_ACCOUNT_BLACK_LIST_FANDOM)

        return Response()
    }


}

package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListAdd
import com.dzen.campfire.server.controllers.ControllerCollisions

class EFandomsBlackListAdd : RFandomsBlackListAdd(0) {
    override fun check() {

    }

    override fun execute(): Response {
        ControllerCollisions.putCollisionWithCheck(apiAccount.id, fandomId, API.COLLISION_ACCOUNT_BLACK_LIST_FANDOM)

        return Response()
    }
}

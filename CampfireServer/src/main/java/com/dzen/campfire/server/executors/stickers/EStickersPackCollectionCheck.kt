package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.stickers.RStickersPackCollectionCheck
import com.dzen.campfire.server.controllers.ControllerCollisions

class EStickersPackCollectionCheck : RStickersPackCollectionCheck(0) {

    override fun check() {

    }

    override fun execute(): Response {

        val inCollection = ControllerCollisions.checkCollisionExist(apiAccount.id, stickersPackId, API.COLLISION_ACCOUNT_STICKERPACKS)

        return Response(inCollection)
    }
}

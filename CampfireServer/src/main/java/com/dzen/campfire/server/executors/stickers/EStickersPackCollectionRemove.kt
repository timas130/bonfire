package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.stickers.RStickersPackCollectionRemove
import com.dzen.campfire.server.controllers.ControllerCollisions

class EStickersPackCollectionRemove : RStickersPackCollectionRemove(0) {

    override fun check() {
    }

    override fun execute(): Response {

        ControllerCollisions.removeCollisions(apiAccount.id, stickersPackId, API.COLLISION_ACCOUNT_STICKERPACKS)

        return Response()
    }
}

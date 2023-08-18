package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.stickers.RStickerCollectionRemove
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.optimizers.OptimizerStickersCount

class EStickerCollectionRemove : RStickerCollectionRemove(0) {

    override fun check() {
    }

    override fun execute(): Response {
        ControllerCollisions.removeCollisions(apiAccount.id, stickerId, API.COLLISION_ACCOUNT_STICKERS)
        OptimizerStickersCount.decrement(apiAccount.id)
        return Response()
    }
}

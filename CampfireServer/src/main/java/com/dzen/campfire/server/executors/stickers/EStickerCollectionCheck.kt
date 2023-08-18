package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.stickers.RStickerCollectionCheck
import com.dzen.campfire.server.controllers.ControllerCollisions

class EStickerCollectionCheck : RStickerCollectionCheck(0) {

    override fun check() {

    }

    override fun execute(): Response {

        val inCollection = ControllerCollisions.checkCollisionExist(apiAccount.id, stickerId, API.COLLISION_ACCOUNT_STICKERS)

        return Response(inCollection)
    }
}

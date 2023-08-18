package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.stickers.RStickerCollectionAdd
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.controllers.ControllerStickers
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.optimizers.OptimizerStickersCount

class EStickerCollectionAdd : RStickerCollectionAdd(0) {

    override fun check() {
        val publication = ControllerPublications.getPublication(stickerId, apiAccount.id)
        if(publication == null) throw ApiException(API.ERROR_GONE)
        if(publication.publicationType != API.PUBLICATION_TYPE_STICKER) throw ApiException(API.ERROR_ACCESS)
        if (ControllerStickers.getStickersCount(apiAccount.id) >= API.STICKERS_MAX_COUNT_ON_ACCOUNT) throw ApiException(E_TOO_MANY)
    }

    override fun execute(): Response {
        ControllerCollisions.putCollisionWithCheck(apiAccount.id, stickerId, API.COLLISION_ACCOUNT_STICKERS)
        OptimizerStickersCount.increment(apiAccount.id)
        return Response()
    }
}
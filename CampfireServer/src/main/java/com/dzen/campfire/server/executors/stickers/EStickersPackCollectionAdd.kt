package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.stickers.RStickersPackCollectionAdd
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.controllers.ControllerStickers
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.api.tools.ApiException

class EStickersPackCollectionAdd : RStickersPackCollectionAdd(0) {

    override fun check() {
        val publication = ControllerPublications.getPublication(stickersPackId, apiAccount.id)
        if (publication == null) throw ApiException(API.ERROR_GONE)
        if (publication.publicationType != API.PUBLICATION_TYPE_STICKERS_PACK) throw ApiException(API.ERROR_ACCESS)
        if (ControllerStickers.getStickersPacksCount(apiAccount.id) >= API.STICKERS_PACK_MAX_COUNT_ON_ACCOUNT) throw ApiException(E_TOO_MANY)
    }

    override fun execute(): Response {

        ControllerCollisions.putCollisionWithCheck(apiAccount.id, stickersPackId, API.COLLISION_ACCOUNT_STICKERPACKS)

        return Response()
    }
}

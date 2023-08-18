package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersPacksGetInfo
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException

class EStickersPacksGetInfo : RStickersPacksGetInfo(0, 0) {

    override fun check() {

    }

    override fun execute(): Response {

        var packId = this.packId

        if (packId == 0L) {
            val v = ControllerPublications.get(stickerId, TPublications.tag_1)
            if (v.isEmpty()) throw ApiException(API.ERROR_GONE)
            packId = v.next()
        }

        val publication = ControllerPublications.getPublication(packId, apiAccount.id)
        if (publication == null) throw ApiException(API.ERROR_GONE)
        publication as PublicationStickersPack
        if (!publication.isPublic) throw ApiException(API.ERROR_GONE)
        return Response(publication)
    }
}
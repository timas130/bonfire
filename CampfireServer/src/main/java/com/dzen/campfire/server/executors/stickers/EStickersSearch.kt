package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersSearch
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database

class EStickersSearch : RStickersSearch(0) {

    override fun check() {
    }

    override fun execute(): Response {

        val select = ControllerPublications.instanceSelect(apiAccount.id)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_STICKERS_PACK)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .count(COUNT)
                .offset(offset)
                .sort(TPublications.NAME + "." + TPublications.karma_count, false)

        val stickersPacks = ControllerPublications.parseSelect(Database.select("EStickersSearch", select))

        return Response(Array(stickersPacks.size) { stickersPacks[it] as PublicationStickersPack })
    }
}

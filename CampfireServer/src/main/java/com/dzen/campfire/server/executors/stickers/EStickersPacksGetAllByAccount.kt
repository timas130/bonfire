package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersPacksGetAllByAccount
import com.dzen.campfire.server.controllers.ControllerStickers
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlWhere

class EStickersPacksGetAllByAccount : RStickersPacksGetAllByAccount(0, 0, 0) {

    override fun check() {
    }

    override fun execute(): Response {

        val ids = ControllerStickers.getStickersPacksIds(accountId)

        if (ids.isEmpty()) return Response(emptyArray())

        val stickersPacks = ControllerPublications.parseSelect(Database.select("EStickersPacksGetAll", ControllerPublications.instanceSelect(apiAccount.id)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_STICKERS_PACK)
                .where(SqlWhere.WhereIN(TPublications.id, ids))
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.date_create, "<", if (offsetDate == 0L) Long.MAX_VALUE else offsetDate)
                .count(count)
                .sort(TPublications.date_create, false)))

        return Response(Array(stickersPacks.size) { stickersPacks[it] as PublicationStickersPack })
    }
}

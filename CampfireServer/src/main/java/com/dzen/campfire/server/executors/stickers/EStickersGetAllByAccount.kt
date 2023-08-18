package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersGetAllByAccount
import com.dzen.campfire.server.controllers.ControllerStickers
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlWhere

class EStickersGetAllByAccount : RStickersGetAllByAccount(0) {

    override fun check() {
    }

    override fun execute(): Response {

        val stickersIds = ControllerStickers.getStickersIds(accountId)
        val stickersPacksIds = ControllerStickers.getStickersPacksIds(accountId)
        var stickers_1 = emptyArray<Publication>()
        var stickers_2 = emptyArray<Publication>()

        if (stickersIds.isNotEmpty()) {
            stickers_1 = ControllerPublications.parseSelect(Database.select("EStickersGetAll [1]", ControllerPublications.instanceSelect(apiAccount.id)
                    .where(SqlWhere.WhereIN(TPublications.id, stickersIds))
                    .where(TPublications.status, "=", API.STATUS_PUBLIC)
                    .where(Sql.IFNULL("(SELECT ${TPublications.status} FROM ${TPublications.NAME} u WHERE u.${TPublications.id}=${TPublications.NAME}.${TPublications.tag_1})", 0), "=", API.STATUS_PUBLIC)
            ))
        }


        if (stickersPacksIds.isNotEmpty()) {
            stickers_2 = ControllerPublications.parseSelect(Database.select("EStickersGetAll [2]", ControllerPublications.instanceSelect(apiAccount.id)
                    .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_STICKER)
                    .where(SqlWhere.WhereIN(TPublications.tag_1, stickersPacksIds))
                    .where(TPublications.status, "=", API.STATUS_PUBLIC)
                    .where(Sql.IFNULL("(SELECT ${TPublications.status} FROM ${TPublications.NAME} u WHERE u.${TPublications.id}=${TPublications.NAME}.${TPublications.tag_1})", 0), "=", API.STATUS_PUBLIC)
            ))
        }

        val stickers = ToolsCollections.merge(stickers_1, stickers_2)

        return Response(Array(stickers.size) { stickers[it] as PublicationSticker })
    }
}
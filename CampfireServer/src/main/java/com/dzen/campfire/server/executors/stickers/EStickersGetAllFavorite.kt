package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.requests.stickers.RStickersGetAllFavorite
import com.dzen.campfire.server.controllers.ControllerStickers
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlWhere

class EStickersGetAllFavorite : RStickersGetAllFavorite(0) {

    override fun check() {
    }

    override fun execute(): Response {

        val stickersId = ControllerStickers.getStickersIds(accountId)

        if (stickersId.isEmpty()) return Response(emptyArray())

        val stickers = ControllerPublications.parseSelect(Database.select("EStickersGetAllFavorite", ControllerPublications.instanceSelect(apiAccount.id)
                .where(SqlWhere.WhereIN(TPublications.id, stickersId))
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(Sql.IFNULL("(SELECT ${TPublications.status} FROM ${TPublications.NAME} u WHERE u.${TPublications.id}=${TPublications.NAME}.${TPublications.tag_1})", 0), "=", API.STATUS_PUBLIC)
                .sort(TPublications.date_create, true)))



        return Response(Array(stickers.size) { stickers[it] as PublicationSticker })
    }
}
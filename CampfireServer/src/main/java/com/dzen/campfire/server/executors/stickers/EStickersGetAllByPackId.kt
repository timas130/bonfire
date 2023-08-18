package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.requests.stickers.RStickersGetAllByPackId
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql

class EStickersGetAllByPackId : RStickersGetAllByPackId(0) {

    override fun check() {
    }

    override fun execute(): Response {

        val select = ControllerPublications.instanceSelect(apiAccount.id)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_STICKER)
                .where(TPublications.tag_1, "=", packId)
                .sort(TPublications.date_create, true)

        if (!ControllerFandom.can(apiAccount, API.LVL_PROTOADMIN)) {
            select.where(TPublications.status, "=", API.STATUS_PUBLIC)
            select.where(Sql.IFNULL("(SELECT ${TPublications.status} FROM ${TPublications.NAME} u WHERE u.${TPublications.id}=${TPublications.NAME}.${TPublications.tag_1})", 0), "=", API.STATUS_PUBLIC)
        }

        val stickers = ControllerPublications.parseSelect(Database.select("EStickersGetAll", select))


        return Response(Array(stickers.size) { stickers[it] as PublicationSticker })
    }
}
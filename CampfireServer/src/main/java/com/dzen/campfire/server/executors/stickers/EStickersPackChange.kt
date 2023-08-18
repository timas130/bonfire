package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersPackChange
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerCensor
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.tools.ToolsImage

class EStickersPackChange : RStickersPackChange(0, "", null) {

    private var publication = PublicationStickersPack()

    override fun check() {
        packName = ControllerCensor.cens(packName)
        ControllerAccounts.checkAccountBanned(apiAccount.id)

        val publication = ControllerPublications.getPublication(publicationId, apiAccount.id) as PublicationStickersPack?

        if (publication == null) throw ApiException(API.ERROR_GONE)
        this.publication = publication

        if (publication.creator.id != apiAccount.id) throw ApiException(API.ERROR_ACCESS)

        if (avatar != null) {
            if (avatar!!.size > API.STICKERS_PACK_IMAGE_WEIGHT) throw ApiException(E_BAD_IMAGE_WEIGHT)
            if (!ToolsImage.checkImageScaleUnknownType(avatar!!, API.STICKERS_PACK_IMAGE_SIDE, API.STICKERS_PACK_IMAGE_SIDE, true, false, true)) throw ApiException(E_BAD_IMAGE_SIZE)
        }

        if (packName.length < API.STICKERS_PACK_NAME_L_MIN || packName.length > API.STICKERS_PACK_NAME_L_MAX) throw ApiException(E_BAD_NAME_SIZE)
        if (!ToolsText.isOnly(packName, API.ENGLISH)) throw ApiException(E_BAD_NAME)
    }

    override fun execute(): Response {

        publication.name = packName

        if (avatar != null) {
            ControllerResources.remove(publication.imageId)
            publication.imageId = ControllerResources.put(avatar!!, API.RESOURCES_PUBLICATION_STICKER_PACK)
        }

        publication.jsonDB = publication.jsonDB(true, Json())

        Database.update("EStickersPackChange", SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.id, "=", publication.id)
                .updateValue(TPublications.publication_json, publication.jsonDB.toString()))

        return Response(publication)
    }
}
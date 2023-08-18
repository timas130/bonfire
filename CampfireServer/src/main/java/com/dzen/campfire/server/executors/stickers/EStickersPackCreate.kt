package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersPackCreate
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java_pc.tools.ToolsImage

class EStickersPackCreate : RStickersPackCreate("", null) {

    override fun check() {
        packName = ControllerCensor.cens(packName)
        ControllerFandom.checkCan(apiAccount, API.LVL_CREATE_STICKERS)

        if (avatar!!.size > API.STICKERS_PACK_IMAGE_WEIGHT) throw ApiException(E_BAD_IMAGE_WEIGHT)
        if (!ToolsImage.checkImageScaleUnknownType(avatar!!, API.STICKERS_PACK_IMAGE_SIDE, API.STICKERS_PACK_IMAGE_SIDE, true, false, true)) throw ApiException(E_BAD_IMAGE_SIZE)

        if (packName.length < API.STICKERS_PACK_NAME_L_MIN || packName.length > API.STICKERS_PACK_NAME_L_MAX) throw ApiException(E_BAD_NAME_SIZE)
        if (!ToolsText.isOnly(packName, API.ENGLISH)) throw ApiException(E_BAD_NAME)

        if (ControllerStickers.getStickersPacksCount(apiAccount.id) >= API.STICKERS_PACK_MAX_COUNT_ON_ACCOUNT) throw ApiException(E_TOO_MANY)
    }

    override fun execute(): Response {

        val publication = PublicationStickersPack()
        publication.name = packName
        publication.imageId = ControllerResources.put(avatar!!, API.RESOURCES_PUBLICATION_STICKER_PACK)
        publication.dateCreate = System.currentTimeMillis()
        publication.creator = ControllerAccounts.instance(apiAccount.id,
                apiAccount.accessTag,
                System.currentTimeMillis(),
                apiAccount.name,
                apiAccount.imageId,
                apiAccount.sex,
                apiAccount.accessTagSub)
        publication.jsonDB = publication.jsonDB(true, Json())
        publication.status = API.STATUS_PUBLIC
        publication.publicationType = API.PUBLICATION_TYPE_STICKERS_PACK

        ControllerPublications.put(publication)

        ControllerCollisions.putCollisionWithCheck(apiAccount.id, publication.id, API.COLLISION_ACCOUNT_STICKERPACKS)

        ControllerPublications.watchComments(apiAccount.id, publication.id, true)

        return Response(publication)
    }
}
package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.requests.stickers.RStickersAdd
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerOptimizer
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.tools.ToolsImage

class EStickersAdd : RStickersAdd(0, null, null) {

    override fun check() {
        ControllerAccounts.checkAccountBanned(apiAccount.id)

        if (image!!.size > API.STICKERS_IMAGE_WEIGHT) throw ApiException(E_BAD_IMG_WEIGHT, " " + image!!.size + " > " + API.STICKERS_IMAGE_WEIGHT)
        if (!ToolsImage.checkImageMaxScaleUnknownType(image!!, API.STICKERS_IMAGE_SIDE, API.STICKERS_IMAGE_SIDE, true, false, true)) throw ApiException(E_BAD_IMG_SIDES)

        if (gif != null) {
            if (gif!!.size > API.STICKERS_IMAGE_WEIGHT_GIF) throw ApiException(E_BAD_IMG_WEIGHT, " " + gif!!.size + " > " + API.STICKERS_IMAGE_WEIGHT_GIF)
            if (!ToolsImage.checkImageMaxScaleUnknownType(gif!!, API.STICKERS_IMAGE_SIDE_GIF, API.STICKERS_IMAGE_SIDE_GIF, false, true, false)) throw ApiException(E_BAD_IMG_SIDES)
        }

        val parentPublication = ControllerPublications.getPublication(packId, apiAccount.id)
        if (parentPublication == null) throw ApiException(API.ERROR_GONE)
        if (parentPublication.creator.id != apiAccount.id) throw ApiException(E_BAD_CREATOR_ID)
        if (parentPublication.publicationType != API.PUBLICATION_TYPE_STICKERS_PACK) throw ApiException(E_BAD_PARENT_PUBLICATION_TYPE)

        val stickersCount:Long = ControllerPublications.get(packId, "(SELECT COUNT(*) FROM ${TPublications.NAME} u WHERE u.${TPublications.tag_1}=${TPublications.NAME}.${TPublications.id} AND u.${TPublications.status}=${API.STATUS_PUBLIC})").next()
        if (stickersCount >= API.STICKERS_MAX_COUNT_IN_PACK) throw ApiException(E_BAD_STICKERS_COUNT)
    }

    override fun execute(): Response {

        val publication = PublicationSticker()
        publication.imageId = ControllerResources.put(image, API.RESOURCES_PUBLICATION_STICKER)
        if (gif != null) publication.imageId = ControllerResources.put(gif, API.RESOURCES_PUBLICATION_STICKER)
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
        publication.tag_1 = packId
        publication.publicationType = API.PUBLICATION_TYPE_STICKER

        ControllerPublications.put(publication)

        return Response(publication)
    }
}
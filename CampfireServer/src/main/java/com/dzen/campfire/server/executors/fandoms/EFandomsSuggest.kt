package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminChangeParams
import com.dzen.campfire.api.requests.fandoms.RFandomsSuggest
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TFandoms
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.tools.ToolsImage

class EFandomsSuggest : RFandomsSuggest("", 0, false, null, null, emptyArray(), emptyArray(), emptyArray(), emptyArray(), "") {

    @Throws(ApiException::class)
    override fun check() {

        if (categoryId != API.CATEGORY_GAMES &&
            categoryId != API.CATEGORY_ANIME &&
            categoryId != API.CATEGORY_MOVIES &&
            categoryId != API.CATEGORY_BOOKS &&
            categoryId != API.CATEGORY_ART &&
            categoryId != API.CATEGORY_RP &&
            categoryId != API.CATEGORY_OTHER
        ) throw ApiException(RFandomsAdminChangeParams.E_BAD_TYPE)
        if (categoryId == API.CATEGORY_OTHER) ControllerFandom.checkCan(apiAccount, API.LVL_PROTOADMIN)

        ControllerAccounts.checkAccountBanned(apiAccount.id)
        if (image!!.size > API.FANDOM_TITLE_IMG_WEIGHT) throw ApiException(E_BAD_IMG_WEIGHT)
        if (imageMini!!.size > API.FANDOM_IMG_WEIGHT) throw ApiException(E_BAD_IMG_MINI_WEIGHT)
        if (!ToolsImage.checkImageScaleUnknownType(image!!, API.FANDOM_TITLE_IMG_W, API.FANDOM_TITLE_IMG_H, true, false, true)) throw ApiException(E_BAD_IMG_SIZE)
        if (!ToolsImage.checkImageScaleUnknownType(imageMini!!, API.FANDOM_IMG_SIDE, API.FANDOM_IMG_SIDE, true, false, true)) throw ApiException(E_BAD_IMG_MINI_SIZE)
        if (name.isEmpty() || name.length > API.FANDOM_NAME_MAX || !ToolsText.isOnly(name, API.ENGLISH)) throw ApiException(E_BAD_NAME_L)
        if (notes.length > API.FANDOM_DESCRIPTION_MAX_L) throw ApiException(E_BAD_NOTES_L)
    }

    override fun execute(): Response {

        val imageMiniId = ControllerResources.put(imageMini!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED)

        val fandomId = Database.insert("EFandomsSuggest",TFandoms.NAME,
                TFandoms.name, name,
                TFandoms.creator_id, apiAccount.id,
                TFandoms.image_title_id, ControllerResources.put(image!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED),
                TFandoms.image_id, imageMiniId,
                TFandoms.date_create, System.currentTimeMillis(),
                TFandoms.fandom_category, categoryId,
                TFandoms.status, API.STATUS_DRAFT,
                TFandoms.fandom_closed, if(closed) 1 else 0)

        ControllerCollisions.putCollisions(fandomId, params1, API.COLLISION_FANDOM_PARAMS_1)
        ControllerCollisions.putCollisions(fandomId, params2, API.COLLISION_FANDOM_PARAMS_2)
        ControllerCollisions.putCollisions(fandomId, params3, API.COLLISION_FANDOM_PARAMS_3)
        ControllerCollisions.putCollisions(fandomId, params4, API.COLLISION_FANDOM_PARAMS_4)
        ControllerCollisions.putCollisionValue2(fandomId, API.COLLISION_FANDOM_SUGGESTION_NOTES, notes)

        return Response()
    }
}

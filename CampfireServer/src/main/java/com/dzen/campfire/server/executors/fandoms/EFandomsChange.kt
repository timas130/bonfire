package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsChange
import com.dzen.campfire.server.controllers.ControllerCensor
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.tools.ToolsImage

class EFandomsChange : RFandomsChange(0, null, null, null, null, null, null, null, null, null) {

    @Throws(ApiException::class)
    override fun check() {
        if (name != null) name = ControllerCensor.cens(name!!)
        if (image != null && image!!.size > API.FANDOM_TITLE_IMG_WEIGHT) throw ApiException(E_BAD_IMG_WEIGHT)
        if (imageMini != null && imageMini!!.size > API.FANDOM_IMG_WEIGHT) throw ApiException(E_BAD_IMG_MINI_WEIGHT)
        if (image != null && !ToolsImage.checkImageScaleUnknownType(image!!, API.FANDOM_TITLE_IMG_W, API.FANDOM_TITLE_IMG_H, true, false, true)) throw ApiException(E_BAD_IMG_SIZE)
        if (imageMini != null && !ToolsImage.checkImageScaleUnknownType(imageMini!!, API.FANDOM_IMG_SIDE, API.FANDOM_IMG_SIDE, true, false, true)) throw ApiException(E_BAD_IMG_MINI_SIZE)
        if (name != null && name!!.isEmpty()) throw ApiException(E_BAD_NAME_L)
    }

    @Throws(ApiException::class)
    override fun execute(): Response {

        val fandom = ControllerFandom.getFandom(fandomId)!!

        if (image != null) ControllerResources.replace(fandom.imageTitleId, image!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED)
        if (imageMini != null) ControllerResources.replace(fandom.imageId, imageMini!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED)

        if (name != null) {
            Database.update("EFandomsChange update_1", SqlQueryUpdate(TFandoms.NAME)
                    .where(TFandoms.id, "=", fandomId)
                    .updateValue(TFandoms.name, name!!))
        }

        if (closed != null) {
            Database.update("EFandomsChange update_2", SqlQueryUpdate(TFandoms.NAME)
                    .where(TFandoms.id, "=", fandomId)
                    .update(TFandoms.fandom_closed, if (closed!!) 1 else 0))
        }

        if (params1 != null && params1!!.isNotEmpty()) {
            ControllerCollisions.removeCollisions(fandomId, API.COLLISION_FANDOM_PARAMS_1)
            ControllerCollisions.putCollisions(fandomId, params1!!, API.COLLISION_FANDOM_PARAMS_1)
        }
        if (params2 != null && params2!!.isNotEmpty()) {
            ControllerCollisions.removeCollisions(fandomId, API.COLLISION_FANDOM_PARAMS_2)
            ControllerCollisions.putCollisions(fandomId, params2!!, API.COLLISION_FANDOM_PARAMS_2)
        }
        if (params3 != null && params3!!.isNotEmpty()) {
            ControllerCollisions.removeCollisions(fandomId, API.COLLISION_FANDOM_PARAMS_3)
            ControllerCollisions.putCollisions(fandomId, params3!!, API.COLLISION_FANDOM_PARAMS_3)
        }
        if (params4 != null && params4!!.isNotEmpty()) {
            ControllerCollisions.removeCollisions(fandomId, API.COLLISION_FANDOM_PARAMS_4)
            ControllerCollisions.putCollisions(fandomId, params4!!, API.COLLISION_FANDOM_PARAMS_4)
        }

        return Response()
    }
}

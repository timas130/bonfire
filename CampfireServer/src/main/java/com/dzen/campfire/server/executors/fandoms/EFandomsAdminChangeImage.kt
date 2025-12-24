package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomChangeAvatar
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomChangeAvatar
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminChangeImage
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerModeration
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.tables.TFandoms
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EFandomsAdminChangeImage : RFandomsAdminChangeImage(0, ByteArray(0), "") {

    var fandom: Fandom? = null

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_AVATAR)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if(fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        Database.update("EFandomsAdminChangeImage 1", SqlQueryUpdate(TFandoms.NAME)
            .where(TFandoms.id, "=", fandomId)
            .update(TFandoms.image_id, ControllerResources.removeAndPut(fandom!!.imageId, image!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED)))

        ControllerPublications.event(ApiEventAdminFandomChangeAvatar(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, fandomId, fandom!!.name, fandom!!.imageId), apiAccount.id)
        ControllerPublications.event(ApiEventFandomChangeAvatar(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, fandomId, fandom!!.name, fandom!!.imageId, comment), apiAccount.id, fandom!!.id, 0)

        return Response()
    }

}

package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomChangeCategory
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomChangeCategory
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminChangeCategory
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EFandomsAdminChangeCategory : RFandomsAdminChangeCategory(0, 0, "") {

    var fandom: Fandom? = null

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_CATEGORY)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        ControllerCollisions.removeCollisions(fandomId, API.COLLISION_FANDOM_PARAMS_1)
        ControllerCollisions.removeCollisions(fandomId, API.COLLISION_FANDOM_PARAMS_2)
        ControllerCollisions.removeCollisions(fandomId, API.COLLISION_FANDOM_PARAMS_3)
        ControllerCollisions.removeCollisions(fandomId, API.COLLISION_FANDOM_PARAMS_4)

        Database.update("EFandomsAdminChangeCategory", SqlQueryUpdate(TFandoms.NAME)
                .where(TFandoms.id, "=", fandom!!.id)
                .update(TFandoms.fandom_category, categoryId))

        ControllerPublications.event(ApiEventAdminFandomChangeCategory(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, fandomId, fandom!!.name, fandom!!.imageId, fandom!!.category, categoryId), apiAccount.id)
        ControllerPublications.event(ApiEventFandomChangeCategory(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, fandomId, fandom!!.name, fandom!!.imageId, comment, fandom!!.category, categoryId), apiAccount.id, fandom!!.id, 0)

        return Response()
    }


}

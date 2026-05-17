package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomRename
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomRename
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminChangeName
import com.dzen.campfire.server.controllers.ControllerCensor
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerModeration
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EFandomsAdminChangeName : RFandomsAdminChangeName(0, "", "") {

    var fandom: Fandom? = null

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_NAME)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        Database.update("EFandomsAdminChangeName", SqlQueryUpdate(TFandoms.NAME)
                .where(TFandoms.id, "=", fandomId)
                .updateValue(TFandoms.name, name))

        ControllerPublications.event(ApiEventAdminFandomRename(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, fandomId, fandom!!.imageId, name, fandom!!.name), apiAccount.id)
        ControllerPublications.event(ApiEventFandomRename(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, fandomId, name, fandom!!.imageId, comment, fandom!!.name), apiAccount.id, fandom!!.id, 0)

        return Response()
    }


}

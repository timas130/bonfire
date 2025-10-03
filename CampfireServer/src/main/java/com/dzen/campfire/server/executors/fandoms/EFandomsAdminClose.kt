package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomClose
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomClose
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminClose
import com.dzen.campfire.server.controllers.ControllerCensor
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerModeration
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EFandomsAdminClose : RFandomsAdminClose(0, false, "") {

    var fandom: Fandom? = null

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_CLOSE)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        Database.update("EFandomsAdminClose update_1", SqlQueryUpdate(TFandoms.NAME)
                .where(TFandoms.id, "=", fandomId)
                .update(TFandoms.fandom_closed, if(closed) 1 else 0))

        Database.update("EFandomsAdminClose update_2", SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.fandom_id, "=", fandomId)
                .update(TPublications.parent_fandom_closed, if(closed) 1 else 0))

        ControllerPublications.event(ApiEventAdminFandomClose(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, fandomId, fandom!!.imageId, fandom!!.name, closed), apiAccount.id)
        ControllerPublications.event(ApiEventFandomClose(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, fandomId, fandom!!.name,  fandom!!.imageId, comment, closed), apiAccount.id, fandom!!.id, 0)

        return Response()
    }


}

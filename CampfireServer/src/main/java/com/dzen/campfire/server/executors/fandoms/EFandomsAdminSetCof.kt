package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomKarmaCofChanged
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomCofChanged
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminSetCof
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EFandomsAdminSetCof : RFandomsAdminSetCof(0, 0, "") {

    var fandom: Fandom? = null

    override fun check() {
        comment = ControllerModeration.parseComment(comment)
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_SET_COF)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        if (cof < API.FANDOM_KARMA_COF_MIN || cof > API.FANDOM_KARMA_COF_MAX) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        ControllerOptimizer.setFandomKarmaCof(fandomId, cof)
        ControllerPublications.event(ApiEventAdminFandomKarmaCofChanged(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, fandomId, fandom!!.imageId, fandom!!.name, fandom!!.karmaCof, cof), apiAccount.id)
        ControllerPublications.event(ApiEventFandomCofChanged(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, fandomId, fandom!!.name, fandom!!.imageId, comment, fandom!!.karmaCof, cof), apiAccount.id, fandom!!.id, 0)

        return Response()
    }


}

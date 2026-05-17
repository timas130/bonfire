package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomChangeParams
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomChangeParams
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminChangeParams
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerModeration
import com.dzen.campfire.server.controllers.ControllerPublications

class EFandomsAdminChangeParams : RFandomsAdminChangeParams(0, 0, 0, emptyArray(), "") {

    var fandom: Fandom? = null

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_PARAMS)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)

        if (fandom!!.category != API.CATEGORY_GAMES &&
            fandom!!.category != API.CATEGORY_ANIME &&
            fandom!!.category != API.CATEGORY_MOVIES &&
            fandom!!.category != API.CATEGORY_BOOKS &&
            fandom!!.category != API.CATEGORY_ART &&
            fandom!!.category != API.CATEGORY_RP &&
            fandom!!.category != API.CATEGORY_OTHER
        ) throw ApiException(E_BAD_TYPE)

        if (paramsPosition < 1 || paramsPosition > 4) throw ApiException(E_BAD_TYPE)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        val listNew = ArrayList<Long>()
        val listRemoved = ArrayList<Long>()
        val currentParams = ControllerFandom.getParams(fandomId, paramsPosition)

        for (i in params.indices) {
            var b = false
            for (element in currentParams) b = b || params[i] == element
            if (!b) listNew.add(params[i])
        }

        for (i in currentParams.indices) {
            var b = false
            for (element in params) b = b || currentParams[i] == element
            if (!b) listRemoved.add(currentParams[i])
        }

        ControllerCollisions.removeCollisions(fandomId, ControllerFandom.getParamsCollisionIndex(paramsPosition))
        ControllerCollisions.putCollisions(fandomId, params, ControllerFandom.getParamsCollisionIndex(paramsPosition))

        ControllerPublications.event(ApiEventAdminFandomChangeParams(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, fandomId, fandom!!.name, fandom!!.imageId, categoryId, paramsPosition, listNew.toTypedArray(), listRemoved.toTypedArray()), apiAccount.id)
        ControllerPublications.event(ApiEventFandomChangeParams(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, fandomId, fandom!!.name, fandom!!.imageId, comment, categoryId, paramsPosition, listNew.toTypedArray(), listRemoved.toTypedArray()), apiAccount.id, fandom!!.id, 0)

        return Response()
    }


}

package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomChangeParams
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomChangeParams
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminChangeParams
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EFandomsAdminChangeParams : RFandomsAdminChangeParams(0, 0, 0, emptyArray(), "") {

    var fandom: Fandom? = null

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_PARAMS)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)

        if (fandom!!.category != API.CATEGORY_GAMES &&
                fandom!!.category != API.CATEGORY_ANIME &&
                fandom!!.category != API.CATEGORY_MUSIC &&
                fandom!!.category != API.CATEGORY_PROGRAMS &&
                fandom!!.category != API.CATEGORY_MOVIES &&
                fandom!!.category != API.CATEGORY_SITE &&
                fandom!!.category != API.CATEGORY_COMPANY &&
                fandom!!.category != API.CATEGORY_BOOKS &&
                fandom!!.category != API.CATEGORY_ANIMALS &&
                fandom!!.category != API.CATEGORY_HOBBIES &&
                fandom!!.category != API.CATEGORY_PEOPLE &&
                fandom!!.category != API.CATEGORY_EVENT &&
                fandom!!.category != API.CATEGORY_PLANTS &&
                fandom!!.category != API.CATEGORY_PLACES &&
                fandom!!.category != API.CATEGORY_OTHER
        ) throw ApiException(E_BAD_TYPE)
        if (fandom!!.category == API.CATEGORY_OTHER) ControllerFandom.checkCan(apiAccount, API.LVL_PROTOADMIN)

        if (paramsPosition < 1 || paramsPosition > 4) throw ApiException(E_BAD_TYPE)
        ControllerModeration.parseComment(comment)
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
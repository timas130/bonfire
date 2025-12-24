package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.moderations.fandom.ModerationNames
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationNames
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EFandomsModerationNames : RFandomsModerationNames(0, 0, emptyArray(), "") {


    override fun check() {
        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_NAMES)
        if (names.size > API.FANDOM_NAMES_MAX) throw ApiException(E_TOO_MANY_ITEMS)
        for (i in names.indices) {
            names[i] = ControllerCensor.cens(names[i])
            if (names[i].length > API.FANDOM_NAMES_MAX_L) throw ApiException(E_BAD_SIZE)
        }
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        val listNew = ArrayList<String>()
        val listRemoved = ArrayList<String>()
        val currentNames = ControllerFandom.getNames(fandomId, languageId)

        for (i in 0 until names.size) {
            var b = false
            for (n in 0 until currentNames.size) b = b || names[i] == currentNames[n]
            if (!b) listNew.add(names[i])
        }

        for (i in 0 until currentNames.size) {
            var b = false
            for (n in 0 until names.size) b = b || currentNames[i] == names[n]
            if (!b) listRemoved.add(currentNames[i])
        }

        var namesStr = ""
        if(names.isNotEmpty()){
            namesStr = names[0]
            for(i in 1 until names.size) namesStr += "~~~" + names[i]
        }

        ControllerCollisions.updateOrCreateValue2(fandomId, languageId, API.COLLISION_FANDOM_NAMES, namesStr)

        ControllerPublications.moderation(ModerationNames(comment, listNew.toTypedArray(), listRemoved.toTypedArray()), apiAccount.id, fandomId, languageId, 0)

        return Response()
    }


}

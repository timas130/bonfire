package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsGetInfo
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.controllers.ControllerFandom

class EFandomsGetInfo : RFandomsGetInfo(0, 0) {

    override fun check() {

    }

    override fun execute(): Response {

        val descriptor = getDescription()

        val params1 = ControllerFandom.getParams(fandomId, 1)
        val params2 = ControllerFandom.getParams(fandomId, 2)
        val params3 = ControllerFandom.getParams(fandomId, 3)
        val params4 = ControllerFandom.getParams(fandomId, 4)

        val response = Response(descriptor,
                ControllerFandom.getCategory(fandomId),
                ControllerFandom.getNames(fandomId, languageId),
                ControllerFandom.getGallery(fandomId, languageId),
                ControllerFandom.getLinks(fandomId, languageId),
                params1,
                params2,
                params3,
                params4
        )

        return response

    }

    private fun getDescription(): String {
        val collisions = ControllerCollisions.getCollisionsValue2(fandomId, languageId, API.COLLISION_FANDOM_DESCRIPTION)
        if (!collisions.isEmpty()) return collisions[0]
        else return ""
    }

}

package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsGetSubscribtion
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerCollisions

class EFandomsGetSubscribtion : RFandomsGetSubscribtion(0, 0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    @Throws(ApiException::class)
    override fun execute(): Response {
        val subscriptionType = ControllerCollisions.getCollisionValue1OrDef(apiAccount.id, fandomId, languageId, API.COLLISION_FANDOM_SUBSCRIBE, 1)
        val notifyImportant = ControllerCollisions.checkCollisionExist(apiAccount.id, fandomId, languageId, API.COLLISION_FANDOM_NOTIFY_IMPORTANT)

        return Response(subscriptionType, notifyImportant)

    }

}

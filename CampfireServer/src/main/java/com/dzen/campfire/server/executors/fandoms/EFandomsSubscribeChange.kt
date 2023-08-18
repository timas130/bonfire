package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsSubscribeChange
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.optimizers.OptimizerSubscribedFandoms


class EFandomsSubscribeChange : RFandomsSubscribeChange(0, 0, 0, false) {

    @Throws(ApiException::class)
    override fun check() {
        if (! API.isLanguageExsit(languageId)) throw ApiException(E_BAD_LANGUAGE)
    }

    override fun execute(): Response {

        if(ControllerCollisions.checkCollisionExist(apiAccount.id, fandomId, languageId, API.COLLISION_FANDOM_SUBSCRIBE)){
            OptimizerSubscribedFandoms.decrement(apiAccount.id)
        }

        ControllerCollisions.removeCollisions(apiAccount.id, fandomId, languageId, API.COLLISION_FANDOM_SUBSCRIBE)
        ControllerCollisions.removeCollisions(apiAccount.id, fandomId, languageId, API.COLLISION_FANDOM_NOTIFY_IMPORTANT)

        if (subscriptionType != API.PUBLICATION_IMPORTANT_NONE) {
            ControllerCollisions.putCollisionValue1(apiAccount.id, fandomId, languageId, API.COLLISION_FANDOM_SUBSCRIBE, subscriptionType)
            if (notifyImportant) ControllerCollisions.putCollisionValue1(apiAccount.id, fandomId, languageId, API.COLLISION_FANDOM_NOTIFY_IMPORTANT, 1)
            OptimizerSubscribedFandoms.increment(apiAccount.id)
        }

        ControllerAccounts.updateSubscribeTag(apiAccount.id)
        ControllerFandom.updateSubscribers(fandomId)

        return Response()
    }



}

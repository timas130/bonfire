package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsSubscribeChange
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.controllers.ControllerFandom


class EFandomsSubscribeChange : RFandomsSubscribeChange(0, 0, 0, false) {

    @Throws(ApiException::class)
    override fun check() {
        if (! API.isLanguageExsit(languageId)) throw ApiException(E_BAD_LANGUAGE)
        if (subscriptionType != API.PUBLICATION_IMPORTANT_IMPORTANT && subscriptionType != API.PUBLICATION_IMPORTANT_DEFAULT && subscriptionType != API.PUBLICATION_IMPORTANT_NONE) throw ApiException(E_BAD_TYPE)
    }

    override fun execute(): Response {
        ControllerCollisions.removeCollisions(apiAccount.id, fandomId, languageId, API.COLLISION_FANDOM_SUBSCRIBE)
        ControllerCollisions.removeCollisions(apiAccount.id, fandomId, languageId, API.COLLISION_FANDOM_NOTIFY_IMPORTANT)

        if (subscriptionType != API.PUBLICATION_IMPORTANT_NONE) {
            ControllerCollisions.putCollisionValue1(apiAccount.id, fandomId, languageId, API.COLLISION_FANDOM_SUBSCRIBE, subscriptionType)
            if (notifyImportant) ControllerCollisions.putCollisionValue1(apiAccount.id, fandomId, languageId, API.COLLISION_FANDOM_NOTIFY_IMPORTANT, 1)
        }

        ControllerAccounts.updateSubscribeTag(apiAccount.id)
        ControllerFandom.updateSubscribers(fandomId)

        return Response()
    }
}

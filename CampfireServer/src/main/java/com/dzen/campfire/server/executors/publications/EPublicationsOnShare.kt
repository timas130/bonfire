package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.publications.RPublicationsOnShare
import com.dzen.campfire.server.controllers.ControllerAchievements
import com.dzen.campfire.server.controllers.ControllerPublications

class EPublicationsOnShare : RPublicationsOnShare(0) {


    override fun execute(): Response {

        if (ControllerPublications.putCollisionWithCheck(publicationId, apiAccount.id, API.COLLISION_SHARE)) {
            ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_CONTENT_SHARE)
        }

        return Response()
    }


}
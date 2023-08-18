package com.dzen.campfire.server.executors.activities

import com.dzen.campfire.api.requests.activities.RActivitiesSubscribeGet
import com.dzen.campfire.server.controllers.ControllerActivities

class EActivitiesSubscribeGet : RActivitiesSubscribeGet(0) {

    override fun check() {

    }

    override fun execute(): Response {
        return Response(ControllerActivities.isSubscribed(apiAccount.id, activityId))
    }


}

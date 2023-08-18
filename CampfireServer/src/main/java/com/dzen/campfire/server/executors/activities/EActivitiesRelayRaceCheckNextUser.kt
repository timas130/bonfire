package com.dzen.campfire.server.executors.activities

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.activities.RActivitiesRelayRaceCheckNextUser
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerActivities

class EActivitiesRelayRaceCheckNextUser : RActivitiesRelayRaceCheckNextUser(0, 0) {

    override fun check() {

    }

    override fun execute(): Response {

        if(nextAccountId  < 1)  return Response()

        val activity = ControllerActivities.getActivity(activityId, apiAccount.id)
        if (activity == null) throw ApiException(API.ERROR_GONE)
        ControllerActivities.checkRelayNextAccount(apiAccount.id, activityId, nextAccountId, activity.fandom.id, activity.fandom.languageId)

        return Response()
    }


}

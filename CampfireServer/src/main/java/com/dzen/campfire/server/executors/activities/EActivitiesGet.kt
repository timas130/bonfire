package com.dzen.campfire.server.executors.activities

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.activities.RActivitiesGet
import com.dzen.campfire.server.controllers.ControllerActivities
import com.dzen.campfire.api.tools.ApiException

class EActivitiesGet() : RActivitiesGet(0) {


    override fun check() {

    }

    override fun execute(): Response {
        val activity = ControllerActivities.getActivity(userActivityId, apiAccount.id)
        if (activity == null) throw  ApiException(API.ERROR_GONE)
        return Response(activity)
    }


}

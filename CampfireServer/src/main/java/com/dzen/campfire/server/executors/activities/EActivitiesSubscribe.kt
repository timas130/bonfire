package com.dzen.campfire.server.executors.activities

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.requests.activities.RActivitiesSubscribe
import com.dzen.campfire.server.controllers.ControllerActivities
import com.dzen.campfire.api.tools.ApiException

class EActivitiesSubscribe : RActivitiesSubscribe(0, false) {

    private var activity = UserActivity()

    override fun check() {
        val activity = ControllerActivities.getActivity(activityId, apiAccount.id)
        if (activity == null) throw ApiException(API.ERROR_GONE)
        this.activity = activity
    }

    override fun execute(): Response {

        if (subscribed) {
            ControllerActivities.setSubscribe(apiAccount.id, activity.id)
        } else {
            ControllerActivities.removeSubscribe(apiAccount.id, activity.id)
        }

        return Response()
    }


}

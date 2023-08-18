package com.dzen.campfire.server.executors.rubrics

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.rubrics.RRubricGet
import com.dzen.campfire.api.requests.rubrics.RRubricsChangeNotifications
import com.dzen.campfire.api.requests.rubrics.RRubricsGetParams
import com.dzen.campfire.server.controllers.ControllerRubrics
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerActivities
import com.dzen.campfire.server.controllers.ControllerCollisions

class ERubricsChangeNotifications : RRubricsChangeNotifications(0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {
        val isNotification =  ControllerCollisions.getCollisionValue1OrDef(apiAccount.id, rubricId,  0L, API.COLLISION_RUBRICS_NOTIFICATIONS, 0L) == 0L

        ControllerCollisions.removeCollisions(apiAccount.id, rubricId, API.COLLISION_RUBRICS_NOTIFICATIONS)
        if(isNotification){
            ControllerCollisions.putCollisionValue1(apiAccount.id, rubricId, API.COLLISION_RUBRICS_NOTIFICATIONS, 1L)
        }

        val rubricsCount = ControllerActivities.getRubricsCount(apiAccount.id)

        return Response(!isNotification, rubricsCount)
    }

}

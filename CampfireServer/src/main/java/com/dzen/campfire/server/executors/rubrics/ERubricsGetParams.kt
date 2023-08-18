package com.dzen.campfire.server.executors.rubrics

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.rubrics.RRubricGet
import com.dzen.campfire.api.requests.rubrics.RRubricsGetParams
import com.dzen.campfire.server.controllers.ControllerRubrics
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerCollisions

class ERubricsGetParams : RRubricsGetParams(0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {
        val isNotification = ControllerCollisions.getCollisionValue1(apiAccount.id, rubricId, API.COLLISION_RUBRICS_NOTIFICATIONS) == 1L
        return Response(isNotification)
    }

}

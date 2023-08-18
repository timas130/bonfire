package com.dzen.campfire.server.executors.rubrics

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.rubrics.RRubricGet
import com.dzen.campfire.server.controllers.ControllerRubrics
import com.dzen.campfire.api.tools.ApiException

class ERubricGet : RRubricGet(0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {

        val rubric = ControllerRubrics.getRubric(rubricId)
        if(rubric == null || rubric.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_GONE)


        return Response(rubric)
    }

}

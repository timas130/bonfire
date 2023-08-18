package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.project.RProjectStatisticErrorsRemove
import com.dzen.campfire.server.controllers.ControllerStatistic
import com.dzen.campfire.api.tools.ApiException

class EProjectStatisticErrorsRemove : RProjectStatisticErrorsRemove("") {

    override fun check() {
        if (apiAccount.id != 1L) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {
        ControllerStatistic.removeError(key)

        return Response()
    }
}
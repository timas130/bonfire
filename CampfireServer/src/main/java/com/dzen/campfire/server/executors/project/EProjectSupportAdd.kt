package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.project.RProjectSupportAdd
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerDonates
import com.sup.dev.java.tools.ToolsDate

class EProjectSupportAdd : RProjectSupportAdd(0,0) {

    val date = ToolsDate.getStartOfMonth()

    override fun check() {
        if(apiAccount.id != 1L) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        ControllerDonates.addDonate(accountId, sum, "mobile", 0)

        return Response()
    }
}
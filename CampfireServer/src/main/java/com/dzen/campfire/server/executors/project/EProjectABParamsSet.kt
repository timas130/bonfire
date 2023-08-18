package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.project.RProjectABParamsSet
import com.dzen.campfire.api.tools.ApiException

class EProjectABParamsSet : RProjectABParamsSet("", "") {

    override fun check() {
        if (apiAccount.id != 1L) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {
        return Response()
    }
}
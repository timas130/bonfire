package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.project.RProjectVersionGet
import com.sup.dev.java.libs.json.Json

class EProjectVersionGet : RProjectVersionGet() {

    override fun execute(): Response {
        return Response(API.VERSION, Json())
    }
}
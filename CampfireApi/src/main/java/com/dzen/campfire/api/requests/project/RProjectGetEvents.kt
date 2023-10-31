package com.dzen.campfire.api.requests.project

import com.dzen.campfire.api.models.project.ProjectEvent
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RProjectGetEvents : Request<RProjectGetEvents.Response>() {
    override fun jsonSub(inp: Boolean, json: Json) {
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {
        var events: Array<ProjectEvent> = arrayOf()

        constructor(json: Json) {
            json(false, json)
        }

        constructor(events: Array<ProjectEvent>) {
            this.events = events
        }

        override fun json(inp: Boolean, json: Json) {
            super.json(inp, json)
        }
    }
}

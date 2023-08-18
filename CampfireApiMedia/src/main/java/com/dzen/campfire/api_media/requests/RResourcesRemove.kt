package com.dzen.campfire.api_media.requests

import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RResourcesRemove(
        var resourceId: Long
) : Request<RResourcesRemove.Response>() {

    override fun jsonSub(inp: Boolean, json: Json) {
        resourceId = json.m(inp, "resourceId", resourceId)
    }

    override fun instanceResponse(json:Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        constructor(json:Json){
            json(false, json)
        }

        constructor()

        override fun json(inp: Boolean, json: Json) {
        }

    }

}
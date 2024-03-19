package com.dzen.campfire.api_media.requests

import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

@Deprecated("use ImageRefs")
open class RResourcesGetJson(
    var resourceId: Long
) : Request<RResourcesGetJson.Response>() {

    init {
        cashAvailable = false
        tokenRequired = false
        tokenDesirable = false
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        resourceId = json.m(inp, "resourceId", resourceId)
    }

    override fun instanceResponse(json:Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        var bytes: String? = null

        constructor(json: Json) {
            json(false, json)
        }

        constructor(bytes: String?) {
            this.bytes = bytes
        }

        override fun json(inp: Boolean, json: Json) {
            bytes = json.mNull(inp, "bytes", bytes, String::class)
        }

    }

}

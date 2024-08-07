package com.dzen.campfire.api.requests.accounts

import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RAccountsBioSetDescription(
        var description: String
) : Request<RAccountsBioSetDescription.Response>() {

    companion object {
        val E_BAD_SIZE = "E_BAD_SIZE"
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        description = json.m(inp, "description", description)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response(
        var bio: String = ""
    ) : Request.Response() {
        constructor(json: Json) : this() {
            json(false, json)
        }

        override fun json(inp: Boolean, json: Json) {
            bio = json.m(inp, "bio", bio)
        }
    }
}

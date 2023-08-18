package com.dzen.campfire.api_media.requests

import com.dzen.campfire.api.tools.client.ApiClient
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RResourcesGetByTag(
        var tag: String
) : Request<RResourcesGetByTag.Response>() {

    init {
        cashAvailable = false
        requestType = ApiClient.REQUEST_TYPE_DATA_LOAD
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        tag = json.m(inp, "tag", tag)
    }

    override fun instanceResponse(data:ByteArray): Response {
        return Response(data)
    }


    class Response(var bytes:ByteArray) : Request.Response() {

        override fun getData():ByteArray? {
            return bytes
        }
    }

}
package com.dzen.campfire.api_media.requests

import com.dzen.campfire.api.tools.client.ApiClient
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

@Deprecated("use ImageRefs")
open class RResourcesGet(
        var resourceId: Long,
        var pwd: String = "",
) : Request<RResourcesGet.Response>() {

    init {
        cashAvailable = false
        requestType = ApiClient.REQUEST_TYPE_DATA_LOAD
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        resourceId = json.m(inp, "resourceId", resourceId)
        pwd = json.m(inp, "pwd", pwd)
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

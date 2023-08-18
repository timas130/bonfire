package com.dzen.campfire.api_media.requests

import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RResourcesPut(
        var resource: ByteArray?,
        var publicationId: Long,
        var resourceId: Long = 0,
        var tag: String = ""
) : Request<RResourcesPut.Response>() {

    init {
        addDataOutput(resource)
    }

    override fun updateDataOutput() {
        resource = dataOutput[0]
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        resourceId = json.m(inp, "resourceId", resourceId)
        publicationId = json.m(inp, "publicationId", publicationId)
        tag = json.m(inp, "tag", tag)
    }

    override fun instanceResponse(json:Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        var resourceId = 0L

        constructor(json:Json){
            json(false, json)
        }

        constructor(resourceId:Long){
            this.resourceId = resourceId
        }

        override fun json(inp: Boolean, json: Json) {
            resourceId = json.m(inp, "resourceId", resourceId)
        }

    }

}
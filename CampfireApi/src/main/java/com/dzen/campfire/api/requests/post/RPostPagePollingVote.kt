package com.dzen.campfire.api.requests.post

import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RPostPagePollingVote(
    var sourceType: Long,
    var sourceId: Long,
    var sourceIdSub: Long,
    var pollingId: Long,
    var itemId: Long
) : Request<RPostPagePollingVote.Response>() {
    companion object {
        const val E_ALREADY = "E_ALREADY"
        const val E_LOW_LEVEL = "E_LOW_LEVEL"
        const val E_LOW_KARMA = "E_LOW_KARMA"
        const val E_LOW_DAYS = "E_LOW_DAYS"
        const val E_ENDED = "E_ENDED"
        const val E_BLACKLISTED = "E_BLACKLISTED"
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        sourceType = json.m(inp, "sourceType", sourceType)
        sourceId = json.m(inp, "sourceId", sourceId)
        sourceIdSub = json.m(inp, "sourceIdSub", sourceIdSub)
        pollingId = json.m(inp, "pollingId", pollingId)
        itemId = json.m(inp, "itemId", itemId)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        constructor(json: Json) {
            json(false, json)
        }

        constructor()

        override fun json(inp: Boolean, json: Json) {

        }

    }

}

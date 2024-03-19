package com.dzen.campfire.api.requests.post

import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

open class RPostPagePollingGetVotes(
    var sourceType: Long,
    var sourceId: Long,
    var sourceIdSub: Long,
    var pollingId: Long,
    var offset: Int,
) : Request<RPostPagePollingGetVotes.Response>() {
    companion object {
        const val COUNT = 20
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        sourceType = json.m(inp, "sourceType", sourceType)
        sourceId = json.m(inp, "sourceId", sourceId)
        sourceIdSub = json.m(inp, "sourceIdSub", sourceIdSub)
        pollingId = json.m(inp, "pollingId", pollingId)
        offset = json.m(inp, "offset", offset)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class PollingResultsItem : JsonParsable, ImageHolder {
        var itemId = 0L
        var account = Account()
        var timestamp = 0L

        override fun json(inp: Boolean, json: Json): Json {
            itemId = json.m(inp, "itemId", itemId)
            account = json.m(inp, "account", account)
            timestamp = json.m(inp, "timestamp", timestamp)
            return json
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            account.fillImageRefs(receiver)
        }
    }

    class Response : Request.Response {
        var results: Array<PollingResultsItem> = emptyArray()

        constructor(json: Json) {
            json(false, json)
        }

        constructor(results: Array<PollingResultsItem>) {
            this.results = results
        }

        override fun json(inp: Boolean, json: Json) {
            results = json.m(inp, "results", results, Array<PollingResultsItem>::class)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            for (result in results) {
                result.fillImageRefs(receiver)
            }
        }
    }
}

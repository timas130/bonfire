package com.dzen.campfire.api.requests.quests

import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.quests.QuestPart
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RQuestsGetParts(
    var id: Long
) : Request<RQuestsGetParts.Response>() {
    override fun jsonSub(inp: Boolean, json: Json) {
        id = json.m(inp, "id", id)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response(
        var parts: Array<QuestPart> = arrayOf(),
        var stateVariables: Json = Json(),
        var stateIndex: Int = 0,
    ) : Request.Response() {
        constructor(json: Json) : this() {
            this.json(false, json)
        }

        override fun json(inp: Boolean, json: Json) {
            parts = json.m(inp, "parts", parts)
            stateVariables = json.m(inp, "stateVariables", stateVariables)
            stateIndex = json.m(inp, "stateIndex", stateIndex)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            for (part in parts) {
                part.fillImageRefs(receiver)
            }
        }
    }
}

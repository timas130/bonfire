package com.dzen.campfire.api.requests.chat

import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RChatMessageChange(
    var publicationId: Long,
    var quoteMessageId: Long,
    var text: String,
    var newFormatting: Boolean
) : Request<RChatMessageChange.Response>() {
    companion object {
        const val E_BAD_TEXT_SIZE = "E_BAD_TEXT_SIZE"
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        publicationId = json.m(inp, "unitId", publicationId)
        quoteMessageId = json.m(inp, "quoteMessageId", quoteMessageId)
        text = json.m(inp, "text", text)
        newFormatting = json.m(inp, "newFormatting", newFormatting)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        var message = PublicationChatMessage()

        constructor(json: Json) {
            json(false, json)
        }

        constructor(message:PublicationChatMessage) {
            this.message = message
        }

        override fun json(inp: Boolean, json: Json) {
            message = json.m(inp, "message", message)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            message.fillImageRefs(receiver)
        }
    }

}

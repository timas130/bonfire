package com.dzen.campfire.api.requests.stickers

import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RStickersGetAllByAccount(
        var accountId: Long
) : Request<RStickersGetAllByAccount.Response>() {

    override fun jsonSub(inp: Boolean, json: Json) {
        accountId = json.m(inp, "accountId", accountId)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        var stickers: Array<PublicationSticker> = emptyArray()

        constructor(json: Json) {
            json(false, json)
        }

        constructor(stickers: Array<PublicationSticker>) {
            this.stickers = stickers
        }

        override fun json(inp: Boolean, json: Json) {
            stickers = json.m(inp, "stickers", stickers)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            for (sticker in stickers) {
                sticker.fillImageRefs(receiver)
            }
        }
    }

}

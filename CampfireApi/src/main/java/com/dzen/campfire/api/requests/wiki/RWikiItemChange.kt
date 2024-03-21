package com.dzen.campfire.api.requests.wiki

import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RWikiItemChange(
        var item: WikiTitle = WikiTitle(),
        var parentItemId:Long,
        var imageMini:ByteArray?,
        var imageBig:ByteArray?
) : Request<RWikiItemChange.Response>() {

    init {
        addDataOutput(imageMini)
        addDataOutput(imageBig)
    }

    override fun updateDataOutput() {
        imageMini = dataOutput[0]
        imageBig = dataOutput[1]
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        parentItemId = json.m(inp, "parentItemId", parentItemId)
        item = json.m(inp, "item", item)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        var item = WikiTitle()

        constructor(json: Json){
            json(false, json)
        }

        constructor(item:WikiTitle){
            this.item = item
        }

        override fun json(inp: Boolean, json: Json) {
            item = json.m(inp, "item", item)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            item.fillImageRefs(receiver)
        }
    }

}

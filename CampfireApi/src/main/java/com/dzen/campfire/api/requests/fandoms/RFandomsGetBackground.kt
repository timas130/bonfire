package com.dzen.campfire.api.requests.fandoms

import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RFandomsGetBackground(
        var fandomId: Long,
        var languageId: Long
) : Request<RFandomsGetBackground.Response>() {

    companion object {
        val COUNT = 20
    }

    init {
        cashAvailable = false
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        fandomId = json.m(inp, "fandomId", fandomId)
        languageId = json.m(inp, "languageId", languageId)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        var imageTitle = ImageRef()
        @Deprecated("use ImageRefs")
        var imageTitleId = 0L
        var imageTitleGif = ImageRef()
        @Deprecated("use ImageRefs")
        var imageTitleGifId = 0L

        constructor(json: Json) {
            json(false, json)
        }

        constructor(imageTitleId: Long, imageTitleGifId:Long) {
            this.imageTitleId = imageTitleId
            this.imageTitleGifId = imageTitleGifId
        }

        override fun json(inp: Boolean, json: Json) {
            imageTitle = json.m(inp, "imageTitle", imageTitle)
            imageTitleId = json.m(inp, "imageTitleId", imageTitleId)
            imageTitleGif = json.m(inp, "imageTitleGif", imageTitleGif)
            imageTitleGifId = json.m(inp, "imageTitleGifId", imageTitleGifId)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            receiver.add(imageTitle, imageTitleId)
            receiver.add(imageTitleGif, imageTitleGifId)
        }
    }

}

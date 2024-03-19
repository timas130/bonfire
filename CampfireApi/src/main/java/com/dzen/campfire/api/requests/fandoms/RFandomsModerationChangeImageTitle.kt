package com.dzen.campfire.api.requests.fandoms

import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RFandomsModerationChangeImageTitle(
        var fandomId: Long,
        var languageId: Long,
        var image: ByteArray?,
        var imageGif: ByteArray?,
        var comment: String
) : Request<RFandomsModerationChangeImageTitle.Response>() {

    companion object {
        val E_BAD_IMG_WEIGHT = "E_BAD_IMG_WEIGHT"
        val E_BAD_IMG_SIDES = "E_BAD_IMG_SIDES"
    }

    init {
        addDataOutput(image)
        addDataOutput(imageGif)
    }

    override fun updateDataOutput() {
        image = dataOutput[0]
        imageGif = dataOutput[1]
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        fandomId = json.m(inp, "fandomId", fandomId)
        languageId = json.m(inp, "languageId", languageId)
        comment = json.m(inp, "comment", comment)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        var image = ImageRef()
        @Deprecated("use ImageRefs")
        var imageId = 0L
        var imageGif = ImageRef()
        @Deprecated("use ImageRefs")
        var imageGifId = 0L

        constructor(json: Json) {
            json(false, json)
        }

        constructor(image: Long, imageGif: Long) {
            this.imageId = image
            this.imageGifId = imageGif
        }

        override fun json(inp: Boolean, json: Json) {
            image = json.m(inp, "image", image)
            imageId = json.m(inp, "imageId", imageId)
            imageGif = json.m(inp, "imageGif", imageGif)
            imageGifId = json.m(inp, "imageGifId", imageGifId)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            receiver.add(image, imageId)
            receiver.add(imageGif, imageGifId)
        }
    }


}

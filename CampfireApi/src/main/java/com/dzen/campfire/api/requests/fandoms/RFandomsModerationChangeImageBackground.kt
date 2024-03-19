package com.dzen.campfire.api.requests.fandoms

import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RFandomsModerationChangeImageBackground(
        var fandomId: Long,
        var languageId: Long,
        var image: ByteArray?,
        var comment: String
) : Request<RFandomsModerationChangeImageBackground.Response>() {

    companion object {
        val E_BAD_IMG_WEIGHT = "E_BAD_IMG_WEIGHT"
        val E_BAD_IMG_SIDES = "E_BAD_IMG_SIDES"
    }

    init {
        addDataOutput(image)
    }

    override fun updateDataOutput() {
        image = dataOutput[0]
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

        constructor(json: Json) {
            json(false, json)
        }

        constructor(image: Long) {
            this.imageId = image
        }

        override fun json(inp: Boolean, json: Json) {
            image = json.m(inp, "image", image)
            imageId = json.m(inp, "imageId", imageId)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            receiver.add(image, imageId)
        }
    }


}

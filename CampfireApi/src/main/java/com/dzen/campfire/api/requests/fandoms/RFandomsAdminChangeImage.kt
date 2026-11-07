package com.dzen.campfire.api.requests.fandoms

import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RFandomsAdminChangeImage(
        var fandomId: Long,
        var image: ByteArray?,
        var comment: String
) : Request<RFandomsAdminChangeImage.Response>() {

    init {
        addDataOutput(image)
    }

    override fun updateDataOutput() {
        image = dataOutput[0]
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        fandomId = json.m(inp, "fandomId", fandomId)
        comment = json.m(inp, "comment", comment)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        var image = ImageRef()
        @Deprecated("use ImageRefs")
        var imageId = 0L

        constructor(json:Json){
            json(false, json)
        }

        constructor(imageId: Long){
            this.imageId = imageId
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
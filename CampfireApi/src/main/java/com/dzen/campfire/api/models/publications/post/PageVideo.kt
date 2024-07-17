package com.dzen.campfire.api.models.publications.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.tools.client.Request
import com.dzen.campfire.api.tools.server.IControllerResources
import com.sup.dev.java.libs.json.Json

class PageVideo : Page() {
    var videoId = ""
    var image = ImageRef()

    @Deprecated("use ImageRefs")
    var imageId = 0L
    var w = 0
    var h = 0
    var insertBytes: ByteArray? = null

    override fun getType() = API.PAGE_TYPE_VIDEO

    override fun addInsertData(request: Request<*>) {
        request.addDataOutput(insertBytes)
    }

    override fun restoreInsertData(request: Request<*>, offset: Int): Int {
        insertBytes = request.dataOutput[0]
        return 1
    }

    override fun duplicateResources(res: IControllerResources, unitId: Long) {
        if (imageId > 0) imageId = res.put(res.get(imageId), unitId)
    }

    override fun json(inp: Boolean, json: Json): Json {
        videoId = json.m(inp, "videoId", videoId)
        image = json.m(inp, "image", image)
        imageId = json.m(inp, "imageId", imageId)
        w = json.m(inp, "J_W", w)
        h = json.m(inp, "J_H", h)
        return super.json(inp, json)
    }

    override fun fillResourcesList(list: ArrayList<Long>) {
        list.add(imageId)
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        super.fillImageRefs(receiver)
        receiver.add(image, imageId, w, h)
    }
}

package com.dzen.campfire.api.models.publications.stickers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.models.publications.Publication
import com.sup.dev.java.libs.json.Json

class PublicationSticker : Publication {
    var image = ImageRef()
    @Deprecated("use ImageRefs")
    var imageId = 0L
    var gif = ImageRef()
    @Deprecated("use ImageRefs")
    var gifId = 0L

    override fun getPublicationTypeConst() = API.PUBLICATION_TYPE_STICKER

    constructor() {

    }

    constructor(jsonDB: Json) : super(jsonDB) {
        restoreFromJsonDB()
    }

    override fun jsonPublication(inp: Boolean, json: Json) {}

    override fun jsonDBLocal(inp: Boolean, json: Json): Json {
        image = json.m(inp, "image", image)
        imageId = json.m(inp, "imageId", imageId)
        gif = json.m(inp, "gif", gif)
        gifId = json.m(inp, "gifId", gifId)
        return json
    }

    override fun fillResourcesList(list: ArrayList<Long>) {
        list.add(imageId)
        list.add(gifId)
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        super.fillImageRefs(receiver)
        receiver.add(image, imageId)
        receiver.add(gif, gifId)
    }
}

package com.dzen.campfire.api.models.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.sup.dev.java.libs.json.Json

class PublicationComment : Publication {
    companion object {
        const val TYPE_TEXT = 0L
        const val TYPE_IMAGE = 3L
        const val TYPE_GIF = 4L
        const val TYPE_IMAGES = 5L
        const val TYPE_STICKER = 6L
    }

    var parentCommentId = 0L
    var type = 0L
    var quoteId = 0L
    var quoteText = ""
    var quoteImages = emptyArray<ImageRef>()

    @Deprecated("use ImageRefs")
    var quoteImageIds = emptyArray<Long>()
    var changed = false
    var quoteStickerId = 0L
    var quoteStickerImage = ImageRef()

    @Deprecated("use ImageRefs")
    var quoteStickerImageId = 0L
    var quoteCreatorName = ""
    var answerName = ""

    var newFormatting = false

    //  TYPE_TEXT
    var text = ""

    //  TYPE_IMAGE
    var image = ImageRef()
    var gif = ImageRef()

    @Deprecated("use ImageRefs")
    var imageId = 0L

    @Deprecated("use ImageRefs")
    var gifId = 0L

    @Deprecated("use ImageRefs")
    var imageW = 0

    @Deprecated("use ImageRefs")
    var imageH = 0


    //  TYPE_IMAGES
    var images = emptyArray<ImageRef>()

    @Deprecated("use ImageRefs")
    var imageIdArray: Array<Long> = emptyArray()

    @Deprecated("use ImageRefs")
    var imageWArray: Array<Int> = emptyArray()

    @Deprecated("use ImageRefs")
    var imageHArray: Array<Int> = emptyArray()

    //  TYPE_STICKER
    var stickerId = 0L
    var stickerImage = ImageRef()

    @Deprecated("use ImageRefs")
    var stickerImageId = 0L
    var stickerGif = ImageRef()

    @Deprecated("use ImageRefs")
    var stickerGifId = 0L

    override fun getPublicationTypeConst() = API.PUBLICATION_TYPE_COMMENT

    constructor()

    constructor(jsonDB: Json) : super(jsonDB) {
        restoreFromJsonDB()
    }

    override fun jsonPublication(inp: Boolean, json: Json) {

    }

    override fun jsonDBLocal(inp: Boolean, json: Json): Json {
        text = json.m(inp, "J_TEXT", text)
        parentCommentId = json.m(inp, "J_PARENT_COMMENT_ID", parentCommentId)
        quoteId = json.m(inp, "quoteId", quoteId)
        quoteText = json.m(inp, "quoteText", quoteText)
        quoteImages = json.m(inp, "quoteImages", quoteImages)
        quoteImageIds = json.m(inp, "quoteImages", quoteImageIds)
        changed = json.m(inp, "changed", changed)
        quoteStickerId = json.m(inp, "quoteStickerId", quoteStickerId)
        quoteStickerImage = json.m(inp, "quoteStickerImage", quoteStickerImage)
        quoteStickerImageId = json.m(inp, "quoteStickerImageId", quoteStickerImageId)
        quoteCreatorName = json.m(inp, "quoteCreatorName", quoteCreatorName)
        answerName = json.m(inp, "answerName", answerName)

        if (inp) json.put("type", type)
        else type = json.getLong("type", TYPE_TEXT)

        image = json.m(inp, "image", image)
        gif = json.m(inp, "gif", gif)
        imageId = json.m(inp, "imageId", imageId)
        gifId = json.m(inp, "gifId", gifId)
        imageW = json.m(inp, "imageW", imageW)
        imageH = json.m(inp, "imageH", imageH)

        images = json.m(inp, "images", images)
        imageIdArray = json.m(inp, "imageIdArray", imageIdArray)
        imageWArray = json.m(inp, "imageWArray", imageWArray)
        imageHArray = json.m(inp, "imageHArray", imageHArray)

        stickerId = json.m(inp, "stickerId", stickerId)
        stickerImage = json.m(inp, "stickerImage", stickerImage)
        stickerImageId = json.m(inp, "stickerImageId", stickerImageId)
        stickerGif = json.m(inp, "stickerGif", stickerGif)
        stickerGifId = json.m(inp, "stickerGifId", stickerGifId)

        newFormatting = json.m(inp, "newFormatting", newFormatting)

        return json
    }

    override fun fillResourcesList(list: ArrayList<Long>) {
        list.add(imageId)
        list.add(gifId)
        for (i in imageIdArray) list.add(i)
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        super.fillImageRefs(receiver)

        receiver.add(quoteStickerImage, quoteStickerImageId)
        receiver.add(image, imageId, imageW, imageH)
        receiver.add(stickerImage, stickerImageId)
        receiver.add(stickerGif, stickerGifId)

        if (quoteImages.isEmpty()) {
            quoteImages = Array(quoteImageIds.size) { ImageRef() }
        }
        if (images.isEmpty()) {
            images = Array(images.size) { ImageRef() }
        }

        for (i in 0 until quoteImages.size.coerceAtLeast(quoteImageIds.size)) {
            receiver.add(quoteImages[i], quoteImageIds[i])
        }
        for (i in 0 until images.size.coerceAtLeast(imageIdArray.size)) {
            receiver.add(images[i], imageIdArray[i], imageWArray[i], imageHArray[i])
        }
    }
}

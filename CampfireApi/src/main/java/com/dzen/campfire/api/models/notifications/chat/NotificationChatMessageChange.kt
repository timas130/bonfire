package com.dzen.campfire.api.models.notifications.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.models.notifications.Notification
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.sup.dev.java.libs.json.Json


class NotificationChatMessageChange : Notification {

    var publicationId: Long = 0
    var text: String = ""
    var newFormatting = false
    var quoteId = 0L
    var quoteText = ""
    var quoteCreatorName = ""
    var quoteImages = emptyArray<ImageRef>()
    var quoteImagesIds: Array<Long> = emptyArray()
    var quoteImagesPwd: Array<String> = emptyArray()
    var quoteStickerId = 0L
    var quoteStickerImage = ImageRef()
    var quoteStickerImageId = 0L

    override fun isShadow() = true

    override fun isNeedForcePush() = false

    override fun getType() = API.NOTIF_CHAT_MESSAGE_CHANGE

    constructor() {

    }

    constructor(message: PublicationChatMessage) {
        setMessage(message)
    }

    fun setMessage(message: PublicationChatMessage) {
        publicationId = message.id
        text = message.text
        newFormatting = message.newFormatting
        quoteId = message.quoteId
        quoteText = message.quoteText
        quoteCreatorName = message.quoteCreatorName
        quoteImages = message.quoteImages
        quoteImagesIds = message.quoteImagesIds
        quoteImagesPwd = message.quoteImagesPwd
        quoteStickerId = message.quoteStickerId
        quoteStickerImage = message.quoteStickerImage
        quoteStickerImageId = message.quoteStickerImageId
    }

    fun applyTo(message: PublicationChatMessage) {
        message.text = text
        message.newFormatting = newFormatting
        message.quoteId = quoteId
        message.quoteText = quoteText
        message.quoteCreatorName = quoteCreatorName
        message.quoteImages = quoteImages
        message.quoteImagesIds = quoteImagesIds
        message.quoteImagesPwd = quoteImagesPwd
        message.quoteStickerId = quoteStickerId
        message.quoteStickerImage = quoteStickerImage
        message.quoteStickerImageId = quoteStickerImageId
        message.changed = true
    }

    override fun json(inp: Boolean, json: Json): Json {
        publicationId = json.m(inp, "J_UNIT_ID", publicationId)
        text = json.m(inp, "J_TEXT", text)
        newFormatting = json.m(inp, "newFormatting", newFormatting)
        quoteId = json.m(inp, "quoteId", quoteId)
        quoteText = json.m(inp, "quoteText", quoteText)
        quoteCreatorName = json.m(inp, "quoteCreatorName", quoteCreatorName)
        quoteImages = json.m(inp, "quoteImageRefs", quoteImages)
        quoteImagesIds = json.m(inp, "quoteImages", quoteImagesIds)
        quoteImagesPwd = json.m(inp, "quoteImagesPwd", quoteImagesPwd)
        quoteStickerId = json.m(inp, "quoteStickerId", quoteStickerId)
        quoteStickerImage = json.m(inp, "quoteStickerImage", quoteStickerImage)
        quoteStickerImageId = json.m(inp, "quoteStickerImageId", quoteStickerImageId)
        return super.json(inp, json)
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        super.fillImageRefs(receiver)
        if (quoteImages.size != quoteImagesIds.size) {
            quoteImages = Array(quoteImagesIds.size) { ImageRef() }
        }
        receiver.add(quoteStickerImage, quoteStickerImageId)
        for (i in quoteImages.indices) {
            receiver.add(quoteImages[i], quoteImagesIds[i])
        }
    }

    override fun fillResourcesList(list: ArrayList<Long>) {

    }

}

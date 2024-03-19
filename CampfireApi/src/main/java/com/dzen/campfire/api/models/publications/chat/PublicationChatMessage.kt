package com.dzen.campfire.api.models.publications.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.models.publications.Publication
import com.sup.dev.java.libs.json.Json

class PublicationChatMessage : Publication {
    companion object {
        const val TYPE_TEXT = 0L
        const val TYPE_SYSTEM = 1L
        const val TYPE_IMAGE = 3L
        const val TYPE_GIF = 4L
        const val TYPE_IMAGES = 5L
        const val TYPE_VOICE = 6L
        const val TYPE_STICKER = 7L

        const val SYSTEM_TYPE_BLOCK = 1L
        const val SYSTEM_TYPE_CREATE = 2L
        const val SYSTEM_TYPE_ADD_USER = 3L
        const val SYSTEM_TYPE_REMOVE_USER = 4L
        const val SYSTEM_TYPE_CHANGE_IMAGE = 5L
        const val SYSTEM_TYPE_CHANGE_NAME = 6L
        const val SYSTEM_TYPE_LEAVE = 7L
        const val SYSTEM_TYPE_ENTER = 8L
        const val SYSTEM_TYPE_PARAMS = 9L
        const val SYSTEM_TYPE_LEVEL = 10L
        const val SYSTEM_TYPE_CHANGE_BACKGROUND = 11L
    }

    var type = 0L
    var chatType = 0L
    var quoteId = 0L
    var quoteText = ""
    var quoteImages = emptyArray<ImageRef>()
    @Deprecated("use ImageRefs")
    var quoteImagesIds: Array<Long> = emptyArray()
    @Deprecated("use ImageRefs")
    var quoteImagesPwd: Array<String> = emptyArray()
    var quoteStickerId = 0L
    var quoteStickerImage = ImageRef()
    @Deprecated("use ImageRefs")
    var quoteStickerImageId = 0L
    var quoteCreatorName = ""
    var changed = false
    var randomTag = 0L
    var answerName = ""
    var newFormatting = false

    //  TYPE_TEXT
    var text: String = ""
    //  TYPE_IMAGE
    var resource = ImageRef()
    @Deprecated("use ImageRefs")
    var resourceId = 0L
    var gif = ImageRef()
    @Deprecated("use ImageRefs")
    var gifId = 0L
    @Deprecated("use ImageRefs")
    var imageW = 0
    @Deprecated("use ImageRefs")
    var imageH = 0
    @Deprecated("use ImageRefs")
    var imagePwd = ""
    //  TYPE_SYSTEM
    var systemType = 0L
    var systemOwnerId = 0L
    var systemOwnerSex = 0L
    var systemOwnerName = ""
    var systemTargetId = 0L
    var systemTargetName = ""
    var systemComment = ""
    var systemTag = 0L
    var blockModerationEventId = 0L
    var blockDate = 0L
    //  TYPE_IMAGES
    var images = emptyArray<ImageRef>()
    @Deprecated("use ImageRefs")
    var imageIdArray: Array<Long> = emptyArray()
    @Deprecated("use ImageRefs")
    var imageWArray: Array<Int> = emptyArray()
    @Deprecated("use ImageRefs")
    var imageHArray: Array<Int> = emptyArray()
    @Deprecated("use ImageRefs")
    var imagePwdArray: Array<String> = emptyArray()
    //  TYPE_VOICE
    var voiceResource = ImageRef()
    @Deprecated("use ImageRefs")
    var voiceResourceId = 0L
    var voiceMs = 0L
    var voiceMask: Array<Int> = emptyArray()
    //  TYPE_STICKER
    var stickerId = 0L
    var stickerImage = ImageRef()
    @Deprecated("use ImageRefs")
    var stickerImageId = 0L
    var stickerGif = ImageRef()
    @Deprecated("use ImageRefs")
    var stickerGifId = 0L

    override fun getPublicationTypeConst() = API.PUBLICATION_TYPE_CHAT_MESSAGE

    constructor()

    constructor(jsonDB: Json) : super(jsonDB) {
        restoreFromJsonDB()
    }

    override fun fillResourcesList(list: ArrayList<Long>) {
        list.add(resourceId)
        list.add(gifId)
        list.add(voiceResourceId)
        for(i in imageIdArray) list.add(i)
    }

    override fun jsonPublication(inp: Boolean, json: Json) {

    }

    override fun jsonDBLocal(inp: Boolean, json: Json): Json {
        text = json.m(inp, "J_TEXT", text)
        newFormatting = json.m(inp, "newFormatting", newFormatting)

        if (inp) json.put("J_TYPE", type)
        else type = json.getLong("J_TYPE", TYPE_TEXT)

        resource = json.m(inp, "resource", resource)
        resourceId = json.m(inp, "J_RESOURCE_ID", resourceId)
        gif = json.m(inp, "gif", gif)
        gifId = json.m(inp, "gifId", gifId)
        imageW = json.m(inp, "J_IMAGE_W", imageW)
        imageH = json.m(inp, "J_IMAGE_H", imageH)
        imagePwd = json.m(inp, "imagePwd", imagePwd)
        chatType = json.m(inp, "chatType", chatType)
        quoteId = json.m(inp, "quoteId", quoteId)
        quoteText = json.m(inp, "quoteText", quoteText)
        quoteImages = json.m(inp, "quoteImageRefs", quoteImages)
        quoteImagesIds = json.m(inp, "quoteImages", quoteImagesIds)
        quoteImagesPwd = json.m(inp, "quoteImagesPwd", quoteImagesPwd)
        changed = json.m(inp, "changed", changed)
        randomTag = json.m(inp, "randomTag", randomTag)
        answerName = json.m(inp, "answerName", answerName)
        quoteStickerId = json.m(inp, "quoteStickerId", quoteStickerId)
        quoteStickerImage = json.m(inp, "quoteStickerImage", quoteStickerImage)
        quoteStickerImageId = json.m(inp, "quoteStickerImageId", quoteStickerImageId)
        quoteCreatorName = json.m(inp, "quoteCreatorName", quoteCreatorName)

        systemType = json.m(inp, "systemType", systemType)
        systemOwnerId = json.m(inp, "systemOwnerId", systemOwnerId)
        systemOwnerSex = json.m(inp, "systemOwnerSex", systemOwnerSex)
        systemOwnerName = json.m(inp, "systemOwnerName", systemOwnerName)
        systemTargetId = json.m(inp, "systemTargetId", systemTargetId)
        systemTargetName = json.m(inp, "systemTargetName", systemTargetName)
        systemComment = json.m(inp, "systemComment", systemComment)
        systemTag = json.m(inp, "systemTag", systemTag)
        blockModerationEventId = json.m(inp, "blockModerationEventId", blockModerationEventId)
        blockDate = json.m(inp, "blockDate", blockDate)

        images = json.m(inp, "images", images)
        imageIdArray = json.m(inp, "imageIdArray", imageIdArray)
        imageWArray = json.m(inp, "imageWArray", imageWArray)
        imageHArray = json.m(inp, "imageHArray", imageHArray)
        imagePwdArray = json.m(inp, "imagePwdArray", imagePwdArray)

        voiceResource = json.m(inp, "voiceResource", voiceResource)
        voiceResourceId = json.m(inp, "voiceResourceId", voiceResourceId)
        voiceMs = json.m(inp, "voiceMs", voiceMs)
        voiceMask = json.m(inp, "voiceMask", voiceMask)

        stickerId = json.m(inp, "stickerId", stickerId)
        stickerImage = json.m(inp, "stickerImage", stickerImage)
        stickerImageId = json.m(inp, "stickerImageId", stickerImageId)
        stickerGif = json.m(inp, "stickerGif", stickerGif)
        stickerGifId = json.m(inp, "stickerGifId", stickerGifId)

        return json
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        super.fillImageRefs(receiver)

        if (quoteImages.isEmpty()) {
            quoteImages = Array(quoteImagesIds.size) { ImageRef() }
        }
        if (images.isEmpty()) {
            images = Array(imageIdArray.size) { ImageRef() }
        }

        receiver.add(resource, resourceId, imageW, imageH)
        for (i in 0 until quoteImages.size.coerceAtLeast(quoteImagesIds.size)) {
            receiver.add(quoteImages[i], quoteImagesIds[i])
        }
        for (i in 0 until images.size.coerceAtLeast(imageIdArray.size)) {
            receiver.add(images[i], imageIdArray[i], imageWArray[i], imageHArray[i])
        }
        receiver.add(voiceResource, voiceResourceId)
        receiver.add(stickerImage, stickerImageId)
        receiver.add(stickerGif, stickerGifId)
    }

    fun chatTag() = ChatTag(chatType, fandom.id, fandom.languageId)
}

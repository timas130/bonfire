package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.requests.chat.RChatMessageCreate
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TChats
import com.dzen.campfire.server.tables.TFandoms
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsCryptography
import com.sup.dev.java_pc.tools.ToolsImage
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.min

class EChatMessageCreate(
        tag: ChatTag,
        text: String,
        images: Array<ByteArray>?,
        gif: ByteArray?,
        voice: ByteArray?,
        parentMessageId: Long
) : RChatMessageCreate(tag, text, images, gif, voice, parentMessageId, 0, 0, false) {
    constructor() : this(ChatTag(), "", null, null, null, 0)

    private var sticker = PublicationSticker()
    private var message = PublicationChatMessage()
    private var parentMessage = PublicationChatMessage()

    @Throws(ApiException::class)
    override fun check() {
        tag.setMyAccountId(apiAccount.id)

        message.creator = ControllerAccounts.instance(
                apiAccount.id,
                0,
                0,
                apiAccount.name,
                apiAccount.imageId,
                apiAccount.sex,
                0
        )
        message.chatType = tag.chatType
        message.fandom.id = tag.targetId
        message.fandom.languageId = tag.targetSubId

        if (message.chatType == API.CHAT_TYPE_FANDOM_ROOT) {
            if (!API.isLanguageExsit(tag.targetSubId)) throw ApiException(API.ERROR_GONE)
            if (ControllerFandom.get(tag.targetId, TFandoms.status).next<Long>() != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
            ControllerAccounts.checkAccountBanned(apiAccount.id, message.fandom.id, message.fandom.languageId)
            val v = ControllerFandom[message.fandom.id, TFandoms.name, TFandoms.image_id, TFandoms.fandom_category]
            message.fandom.name = v.next()
            message.fandom.imageId = v.next()
            message.category = v.next()

        } else if (message.chatType == API.CHAT_TYPE_PRIVATE) {
            ControllerAccounts.checkAccountBanned(apiAccount.id)
            val v = ControllerAccounts.get(message.fandom.id, TAccounts.name, TAccounts.img_id)
            message.fandom.name = v.next()
            message.fandom.imageId = v.next()
        } else if (message.chatType == API.CHAT_TYPE_CONFERENCE) {
            ControllerAccounts.checkAccountBanned(apiAccount.id)

            val memberStatus = ControllerChats.getMemberStatus(apiAccount.id, tag.targetId)
            if (memberStatus == API.CHAT_MEMBER_STATUS_LEAVE) {
                ControllerChats.enter(apiAccount, tag)
                ControllerChats.putEnter(apiAccount, tag)
            }

            if (!ControllerChats.hasAccessToConf_Write(apiAccount.id, tag.targetId)) throw ApiException(API.ERROR_ACCESS)
            val v = ControllerChats.getChat(message.fandom.id, TChats.name, TChats.image_id)
            message.fandom.name = v.next()
            message.fandom.imageId = v.next()
        } else if (message.chatType == API.CHAT_TYPE_FANDOM_SUB) {
            ControllerAccounts.checkAccountBanned(apiAccount.id, message.fandom.id, message.fandom.languageId)
            val v = ControllerFandom[message.fandom.id, TFandoms.name, TFandoms.image_id, TFandoms.fandom_category]
            message.fandom.name = v.next()
            message.fandom.imageId = v.next()
            message.category = v.next()

        }else {
            throw ApiException(E_BAD_CHAT_TYPE)
        }

        text = ControllerCensor.cens(text)

        if (imageArray == null && voice == null && text.isEmpty() && stickerId <= 0) throw ApiException(E_BAD_DATA)
        if (gif != null && (imageArray == null || imageArray!!.size != 1)) throw ApiException(E_BAD_DATA)
        if (imageArray != null && imageArray!!.isEmpty()) throw ApiException(E_BAD_DATA)
        if (parentMessageId > 0) {
            val parentMessageX = ControllerPublications.getPublication(parentMessageId, apiAccount.id) as PublicationChatMessage?
            if(parentMessageX != null) parentMessage = parentMessageX
        }
        if (stickerId <= 0 && voice == null && ((imageArray == null && text.length < API.CHAT_MESSAGE_TEXT_MIN_L) || text.length > API.CHAT_MESSAGE_TEXT_MAX_L)) throw ApiException(E_BAD_TEXT)
        if ((imageArray != null || text.isNotEmpty() || gif != null) && voice != null) throw ApiException(E_BAD_DATA)

        if (imageArray != null) {
            if (imageArray!!.size > 1) {
                message.imageWArray = Array(imageArray!!.size) { 0 }
                message.imageHArray = Array(imageArray!!.size) { 0 }
            }
            for (i in 0 until imageArray!!.size) {
                if (!ToolsBytes.isGif(imageArray!![i]) && imageArray!![i].size > API.CHAT_MESSAGE_IMAGE_WEIGHT) throw ApiException(E_BAD_IMAGE)
                if (ToolsBytes.isGif(imageArray!![i]) && imageArray!![i].size > API.CHAT_MESSAGE_GIF_MAX_WEIGHT) throw ApiException(E_BAD_IMAGE)
                val scale = ToolsImage.getImgScaleUnknownType(imageArray!![i], true, true, true)
                message.imageW = scale[0]
                message.imageH = scale[1]
                if (imageArray!!.size > 1) {
                    message.imageWArray[i] = scale[0]
                    message.imageHArray[i] = scale[1]
                }
                if (!ToolsBytes.isGif(imageArray!![i]) && (scale[0] > API.CHAT_MESSAGE_IMAGE_SIDE || scale[1] > API.CHAT_MESSAGE_IMAGE_SIDE)) throw ApiException(E_BAD_IMAGE)
                if (ToolsBytes.isGif(imageArray!![i]) && (scale[0] > API.CHAT_MESSAGE_IMAGE_SIDE_GIF || scale[1] > API.CHAT_MESSAGE_IMAGE_SIDE_GIF)) throw ApiException(E_BAD_IMAGE)
            }
        }
        if (gif != null) {
            if (!ToolsBytes.isGif(gif)) throw ApiException(E_BAD_GIF)
            if (gif!!.size > API.CHAT_MESSAGE_GIF_MAX_WEIGHT) throw ApiException(E_BAD_GIF)
            val scale = ToolsImage.getImgScaleUnknownType(gif!!, true, true, true)
            if (scale[0] > API.CHAT_MESSAGE_IMAGE_SIDE_GIF || scale[1] > API.CHAT_MESSAGE_IMAGE_SIDE_GIF) throw ApiException(E_BAD_IMAGE)
            val scale2 = ToolsImage.getImgScaleUnknownType(imageArray!![0], true, true, true)
            if (scale2[0] > API.CHAT_MESSAGE_IMAGE_SIDE_GIF || scale2[1] > API.CHAT_MESSAGE_IMAGE_SIDE_GIF) throw ApiException(E_BAD_IMAGE)
        }
        if (stickerId != 0L) {
            sticker = ControllerPublications.getPublication(stickerId, apiAccount.id) as PublicationSticker
            if (sticker.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        }

        if (tag.chatType == API.CHAT_TYPE_PRIVATE) {
            if (ControllerCollisions.checkCollisionExist(tag.getAnotherId(), apiAccount.id, API.COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT)) throw ApiException(E_BLACK_LIST)
        }

    }

    override fun execute(): Response {
        message.newFormatting = newFormatting
        message.text = text

        when {
            gif != null -> parseGif()
            imageArray != null -> parseImage()
            voice != null -> parseVoice()
            stickerId != 0L -> parseSticker()
            else -> message.type = PublicationChatMessage.TYPE_TEXT
        }

        if (quoteMessageId != 0L) {
            val quoteMessage = ControllerPublications.getPublication(quoteMessageId, apiAccount.id)
            if (quoteMessage != null && quoteMessage is PublicationChatMessage && quoteMessage.chatTag() == tag) {
                message.quoteId = quoteMessage.id
                message.quoteText = quoteMessage.creator.name + ": " + quoteMessage.text
                message.quoteCreatorName = quoteMessage.creator.name
                if (message.quoteText.length > API.CHAT_MESSAGE_QUOTE_MAX_SIZE) message.quoteText = message.quoteText.substring(0, API.CHAT_MESSAGE_QUOTE_MAX_SIZE) + "..."
                if (quoteMessage.imageIdArray.isNotEmpty()) {
                    message.quoteImagesIds = quoteMessage.imageIdArray
                    message.quoteImagesPwd = quoteMessage.imagePwdArray
                } else if (quoteMessage.resourceId > 0) {
                    message.quoteImagesIds = Array(1) { quoteMessage.resourceId }
                    message.quoteImagesPwd = arrayOf(quoteMessage.imagePwd)
                } else if (quoteMessage.stickerId > 0) {
                    message.quoteStickerId = quoteMessage.stickerId
                    message.quoteStickerImageId = quoteMessage.stickerImageId
                } else message.quoteImagesIds = emptyArray()
            }
        }

        if (stickerId > 0 && message.text.isEmpty() && parentMessage.id > 0 && quoteMessageId == 0L) {
            message.text = parentMessage.creator.name
        }
        if (parentMessage.id > 0) {
            message.answerName = parentMessage.creator.name
        }

        message.publicationType = API.PUBLICATION_TYPE_CHAT_MESSAGE
        message.parentPublicationId = parentMessage.id
        message.creator.imageId = apiAccount.imageId
        message.creator.name = apiAccount.name
        message.creator.sex = apiAccount.sex
        message.creator.lvl = apiAccount.accessTag
        message.creator.karma30 = apiAccount.accessTagSub
        message.creator.lastOnlineDate = System.currentTimeMillis()

        message.jsonDB = message.jsonDB(true, Json())

        ControllerChats.putMessage(apiAccount, message, tag)

        if (tag.chatType == API.CHAT_TYPE_PRIVATE && tag.getAnotherId() == API.ACCOUNT_CONTENT_GUY_ID) {
            ControllerSubThread.inSub("ControllerChatBot", apiAccount.id) {
                ControllerChatBot.handleMessage(apiAccount.id, text.trim())
            }
        }

        return Response(message.id, message)
    }

    private fun parseGif() {
        message.type = PublicationChatMessage.TYPE_GIF
        message.imagePwd = ToolsCryptography.generateString(10)
        message.gifId = ControllerResources.put(gif!!, API.RESOURCES_PUBLICATION_CHAT_MESSAGE, message.imagePwd)
        message.resourceId = ControllerResources.put(imageArray!![0], API.RESOURCES_PUBLICATION_CHAT_MESSAGE, message.imagePwd)
    }

    private fun parseImage() {
        if (imageArray!!.size == 1) {
            message.type = PublicationChatMessage.TYPE_IMAGE
            message.imagePwd = ToolsCryptography.generateString(10)
            message.resourceId = ControllerResources.put(imageArray!![0], API.RESOURCES_PUBLICATION_CHAT_MESSAGE, message.imagePwd)
        } else {
            message.type = PublicationChatMessage.TYPE_IMAGES
            message.imagePwdArray = Array(imageArray!!.size) { ToolsCryptography.generateString(10) }
            message.imageIdArray = Array(imageArray!!.size) {
                ControllerResources.put(imageArray!![it], API.RESOURCES_PUBLICATION_CHAT_MESSAGE, message.imagePwdArray[it])
            }
        }
    }

    private fun parseVoice() {
        val maxLines = 30

        if (tag.chatType == API.CHAT_TYPE_PRIVATE) {
            val accountSettings = ControllerAccounts.getSettings(tag.targetId)
            if (accountSettings.voiceMessagesIgnore) throw ApiException(E_IS_IGNORE_VOICE_MESSAGES)
        }

        message.type = PublicationChatMessage.TYPE_VOICE
        message.voiceResourceId = ControllerResources.put(voice, API.RESOURCES_PUBLICATION_CHAT_MESSAGE)
        message.voiceMs = (voice!!.size / 17f).toLong()
        if (message.voiceMs > API.CHAT_MESSAGE_VOICE_MAX_MS) throw ApiException(E_BAD_DATA)
        if (voice!!.size > maxLines) {
            val step = voice!!.size / maxLines
            message.voiceMask = Array(maxLines) { 0 }
            var index = 0
            for (i in 0 until voice!!.size step step) {
                var sum = 0L
                for (n in i until min(voice!!.size, i + step) step 8) {
                    val byteBuffer = ByteBuffer.allocate(8)
                    byteBuffer.put(voice!![i])
                    byteBuffer.put(voice!![i + 1])
                    byteBuffer.put(voice!![i + 2])
                    byteBuffer.put(voice!![i + 3])
                    byteBuffer.put(voice!![i + 4])
                    byteBuffer.put(voice!![i + 5])
                    byteBuffer.put(voice!![i + 6])
                    byteBuffer.put(voice!![i + 7])
                    sum += abs(ByteBuffer.wrap(byteBuffer.array()).long)
                }
                message.voiceMask[index] = abs((sum / step).toInt())
                index++
                if (index >= maxLines) break
            }
        } else {
            message.voiceMask = Array(voice!!.size) { abs(voice!![it].toInt()) }
        }
    }

    private fun parseSticker() {
        message.type = PublicationChatMessage.TYPE_STICKER
        message.stickerId = stickerId
        message.stickerImageId = sticker.imageId
        message.stickerGifId = sticker.gifId
    }
}

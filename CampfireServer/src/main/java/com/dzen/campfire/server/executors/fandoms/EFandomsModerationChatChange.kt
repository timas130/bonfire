package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatParamsFandomSub
import com.dzen.campfire.api.models.publications.moderations.chat.ModerationChatChange
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationChatChange
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TChats
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.tools.ToolsImage

class EFandomsModerationChatChange : RFandomsModerationChatChange(0, "", "", "", null) {

    var fandomId = 0L
    var languageId = 0L
    var imageId = 0L
    var nameOld = ""

    @Throws(ApiException::class)
    override fun check() {
        name = ControllerCensor.cens(name)
        text = ControllerCensor.cens(text)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)

        val v = Database.select("EFandomsModerationChatChange select", SqlQuerySelect(TChats.NAME, TChats.fandom_id, TChats.language_id, TChats.name, TChats.image_id)
                .where(TChats.id, "=", chatId)
        )

        if(v.isEmpty) throw ApiException(API.ERROR_GONE)

        fandomId = v.next()
        languageId = v.next()
        nameOld = v.next()
        imageId = v.next()

        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_CHATS)


        if (image != null) {
            if (ToolsBytes.isGif(image) && image!!.size > API.CHAT_IMG_WEIGHT_GIF) throw ApiException(E_BAD_IMAGE_WEIGHT)
            if (!ToolsBytes.isGif(image) && image!!.size > API.CHAT_IMG_WEIGHT) throw ApiException(E_BAD_IMAGE_WEIGHT)
            if (ToolsBytes.isGif(image) &&!ToolsImage.checkImageScaleUnknownType(image!!, API.CHAT_IMG_SIDE_GIF, API.CHAT_IMG_SIDE_GIF, true, true, true)) throw ApiException(E_BAD_IMAGE_SIZE)
            if (!ToolsBytes.isGif(image) &&!ToolsImage.checkImageScaleUnknownType(image!!, API.CHAT_IMG_SIDE, API.CHAT_IMG_SIDE, true, false, true)) throw ApiException(E_BAD_IMAGE_SIZE)
        }

        if (name.length < API.CHAT_NAME_MIN || name.length > API.CHAT_NAME_MAX) throw ApiException(E_BAD_NAME_SIZE)
        if (text.length < API.FANDOM_CHAT_TEXT_MIN_L || text.length > API.FANDOM_CHAT_TEXT_MAX_L) throw ApiException(E_BAD_TEXT_SIZE)

    }

    override fun execute(): Response {

        val update = SqlQueryUpdate(TChats.NAME)
                .where(TChats.id, "=", chatId)

        if(name.isNotEmpty()){
            update.updateValue(TChats.name, name)
        }

        if(text.isNotEmpty()){
            update.updateValue(TChats.chat_params, ChatParamsFandomSub(text).json(true, Json()))
        }

        if(image != null){
            update.update(TChats.image_id, ControllerResources.removeAndPut(imageId, image!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED))
        }

        Database.update("EFandomsModerationChatChange update", update)
        ControllerPublications.moderation(ModerationChatChange(comment, chatId, name), apiAccount.id, fandomId, languageId, 0)

        return Response()
    }

}

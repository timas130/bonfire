package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatParamsFandomSub
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.moderations.chat.ModerationChatCreate
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationChatCreate
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TChats
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.tools.ToolsImage

class EFandomsModerationChatCreate : RFandomsModerationChatCreate(0, 0, "", "", "", null) {

    @Throws(ApiException::class)
    override fun check() {
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
        name = ControllerCensor.cens(name)
        text = ControllerCensor.cens(text)
        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_CHATS)

        if (ToolsBytes.isGif(image) && image!!.size > API.CHAT_IMG_WEIGHT_GIF) throw ApiException(E_BAD_IMAGE_WEIGHT)
        if (!ToolsBytes.isGif(image) && image!!.size > API.CHAT_IMG_WEIGHT) throw ApiException(E_BAD_IMAGE_WEIGHT)
        if (ToolsBytes.isGif(image) && !ToolsImage.checkImageScaleUnknownType(image!!, API.CHAT_IMG_SIDE_GIF, API.CHAT_IMG_SIDE_GIF, true, true, true)) throw ApiException(E_BAD_IMAGE_SIZE)
        if (!ToolsBytes.isGif(image) && !ToolsImage.checkImageScaleUnknownType(image!!, API.CHAT_IMG_SIDE, API.CHAT_IMG_SIDE, true, false, true)) throw ApiException(E_BAD_IMAGE_SIZE)


        if (name.isEmpty() || name.length > API.FANDOM_NAME_MAX) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        val id = Database.insert("EFandomsModerationChatCreate", TChats.NAME,
                TChats.type, API.CHAT_TYPE_FANDOM_SUB,
                TChats.date_create, System.currentTimeMillis(),
                TChats.name, name,
                TChats.fandom_id, fandomId,
                TChats.language_id, languageId,
                TChats.creator_id, apiAccount.id,
                TChats.image_id, ControllerResources.put(image, API.RESOURCES_PUBLICATION_DATABASE_LINKED),
                TChats.chat_params, ChatParamsFandomSub(text).json(true, Json())
        )

        val tag = ChatTag(API.CHAT_TYPE_FANDOM_SUB, id, 0)
        ControllerChats.updateReadOrSubscribe(apiAccount.id, tag, 1,  API.CHAT_MEMBER_LVL_USER, 0, System.currentTimeMillis())

        ControllerPublications.moderation(ModerationChatCreate(comment, id, name), apiAccount.id, fandomId, languageId, 0)

        ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_CREATE_FANDOM_CHAT)
        ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_CREATE_CHAT)

        return Response(tag)
    }

}

package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.requests.chat.RChatCreate
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerCensor
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.tables.TChats
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.tools.ToolsImage

class EChatCreate : RChatCreate("", null) {

    override fun check() {
        name = ControllerCensor.cens(name)
        ControllerAccounts.checkAccountBanned(apiAccount.id)
        if (image!!.size > API.CHAT_IMG_WEIGHT) throw ApiException(API.ERROR_ACCESS)
        if (!ToolsImage.checkImageScaleUnknownType(image!!, API.CHAT_IMG_SIDE, API.CHAT_IMG_SIDE, true, false, true)) throw ApiException(API.ERROR_ACCESS)
        if (name.isEmpty() || name.length > API.FANDOM_NAME_MAX) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        val id = Database.insert("EChatCreate", TChats.NAME,
                TChats.type, API.CHAT_TYPE_CONFERENCE,
                TChats.date_create, System.currentTimeMillis(),
                TChats.name, name,
                TChats.fandom_id, 0,
                TChats.language_id, 0,
                TChats.creator_id, apiAccount.id,
                TChats.image_id, ControllerResources.put(image, API.RESOURCES_PUBLICATION_DATABASE_LINKED)
        )

        val tag = ChatTag(API.CHAT_TYPE_CONFERENCE, id, 0)
        ControllerChats.updateReadOrSubscribe(apiAccount.id, tag, 1,  API.CHAT_MEMBER_LVL_ADMIN, 0, System.currentTimeMillis())
        ControllerChats.putCreationEvent(apiAccount, tag)

        return Response(tag)
    }


}
package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.moderations.fandom.ModerationBackgroundImageSub
import com.dzen.campfire.api.requests.chat.RChatSetBackgroundImage
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TChats
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.tools.ToolsImage

class EChatSetBackgroundImage : RChatSetBackgroundImage(0L, null) {


    override fun check() {
        ControllerAccounts.checkAccountBanned(apiAccount.id)
        if (image != null) {
            if (image!!.size > API.CHAT_IMG_BACKGROUND_WEIGHT) throw ApiException(E_BAD_IMG_WEIGHT, " " + image!!.size + " > " + API.ACCOUNT_IMG_WEIGHT)
            if (!ToolsImage.checkImageMaxScaleUnknownType(image!!, API.CHAT_IMG_BACKGROUND_W, API.CHAT_IMG_BACKGROUND_H, true, true, true)) throw ApiException(E_BAD_IMG_SIDES)
        }
    }

    override fun execute(): Response {

        val v = Database.select("EChatSetBackgroundImage select", SqlQuerySelect(TChats.NAME,
                TChats.type,
                TChats.fandom_id,
                TChats.language_id,
                TChats.background_id,
                TChats.name)
                .where(TChats.id, "=", chatId)
        )

        if(!v.hasNext()) throw ApiException(API.ERROR_GONE)

        val type:Long = v.next()
        val fandomId:Long = v.next()
        val languageId:Long = v.next()
        var backgroundId:Long = v.next()
        val name:String = v.next()

        if(type == API.CHAT_TYPE_FANDOM_SUB){
            ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_CHATS)
        }else if(type == API.CHAT_TYPE_CONFERENCE){
            if(!ControllerChats.hasAccessToConf_Edit(apiAccount.id, chatId)) throw ApiException(API.ERROR_ACCESS)
        }else{
            throw ApiException(API.ERROR_ACCESS)
        }

        if(backgroundId != 0L) ControllerResources.remove(backgroundId)
        if(image != null) backgroundId = ControllerResources.put(image, API.RESOURCES_PUBLICATION_DATABASE_LINKED)
        else backgroundId = 0

        Database.update("EChatSetBackgroundImage update", SqlQueryUpdate(TChats.NAME)
                .where(TChats.id, "=", chatId)
                .update(TChats.background_id, backgroundId)
        )

        if(type == API.CHAT_TYPE_FANDOM_SUB){
            ControllerPublications.moderation(ModerationBackgroundImageSub("", backgroundId, chatId, name), apiAccount.id, fandomId, languageId, 0)
        }else if(type == API.CHAT_TYPE_CONFERENCE){
            ControllerChats.putChangeBackground(apiAccount, ChatTag(API.CHAT_TYPE_CONFERENCE, chatId, 0), backgroundId)
        }else{
            throw ApiException(API.ERROR_ACCESS)
        }

        return Response(backgroundId)
    }

}
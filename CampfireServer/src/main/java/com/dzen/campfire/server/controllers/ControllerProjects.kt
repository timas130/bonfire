package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.tools.ApiAccount

object ControllerProjects {


    fun initAccountForProject(account: ApiAccount, languageId:Long, projectKey: String) {
        val exist = ControllerCollisions.checkCollisionExist(account.id, null, null, API.COLLISION_ACCOUNT_PROJECT_INIT, null, null, projectKey, null, null, null)

        if (exist) return

        var inited = false
        if(projectKey == API.PROJECT_KEY_CAMPFIRE){
            inited = true
            ControllerChats.createSubscriptionIfNotExist(account.id, ChatTag(API.CHAT_TYPE_FANDOM_ROOT, API.FANDOM_CAMPFIRE_ID, languageId), 0, API.CHAT_MEMBER_LVL_USER, 0, System.currentTimeMillis())
            ControllerChats.updateLastMessage(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, API.FANDOM_CAMPFIRE_ID, languageId))
        }
        if(projectKey == API.PROJECT_KEY_CAMPFIRE_FORTNITE){
            inited = true
            ControllerChats.createSubscriptionIfNotExist(account.id, ChatTag(API.CHAT_TYPE_FANDOM_ROOT, API.FANDOM_CAMPFIRE_FORTNITE_ID, languageId), 0, API.CHAT_MEMBER_LVL_USER, 0, System.currentTimeMillis())
            ControllerChats.updateLastMessage(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, API.FANDOM_CAMPFIRE_FORTNITE_ID, languageId))
        }

        if(projectKey == API.PROJECT_KEY_CAMPFIRE_TERRARIA){
            inited = true
            ControllerChats.createSubscriptionIfNotExist(account.id, ChatTag(API.CHAT_TYPE_FANDOM_ROOT, API.FANDOM_CAMPFIRE_TERRARIA_ID, languageId), 0, API.CHAT_MEMBER_LVL_USER, 0, System.currentTimeMillis())
            ControllerChats.updateLastMessage(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, API.FANDOM_CAMPFIRE_TERRARIA_ID, languageId))
        }
        if(projectKey == API.PROJECT_KEY_CAMPFIRE_PUBG){
            inited = true
            ControllerChats.createSubscriptionIfNotExist(account.id, ChatTag(API.CHAT_TYPE_FANDOM_ROOT, API.FANDOM_CAMPFIRE_PUBG_ID, languageId), 0, API.CHAT_MEMBER_LVL_USER, 0, System.currentTimeMillis())
            ControllerChats.updateLastMessage(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, API.FANDOM_CAMPFIRE_PUBG_ID, languageId))
        }

        if(projectKey == API.PROJECT_KEY_CAMPFIRE_ANIME){
            inited = true
            ControllerChats.createSubscriptionIfNotExist(account.id, ChatTag(API.CHAT_TYPE_FANDOM_ROOT, API.FANDOM_CAMPFIRE_ANIME_ID, languageId), 0, API.CHAT_MEMBER_LVL_USER, 0, System.currentTimeMillis())
            ControllerChats.updateLastMessage(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, API.FANDOM_CAMPFIRE_ANIME_ID, languageId))
        }

        if(inited){
            ControllerCollisions.putCollision(account.id, null, null,  API.COLLISION_ACCOUNT_PROJECT_INIT, null, null, projectKey)
        }

    }

}
package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.server.executors.chat.EChatMessageCreate
import com.dzen.campfire.server.rust.RustDailyTask
import com.sup.dev.java.tools.ToolsText

object ControllerChatBot {
    private fun respondTo(fromAccount: Long, text: String) {
        val tag = ChatTag(API.CHAT_TYPE_PRIVATE, fromAccount, API.ACCOUNT_CONTENT_GUY_ID)
        val botAccount = ApiAccount(
            id = API.ACCOUNT_CONTENT_GUY_ID,
            imageId = 1L,
            name = "ContentGuy",
            lastOnlineTime = System.currentTimeMillis()
        )

        ControllerChats.markRead(botAccount.id, tag)

        val req = EChatMessageCreate()
        req.tag = tag
        req.text = text
        req.newFormatting = true
        req.apiAccount = botAccount
        req.check()
        req.execute()
    }

    fun handleMessage(fromAccount: Long, text: String) {
        when (text) {
            "fandoms", "фэндомы" -> {
                val fandoms = RustDailyTask.getPossibleFandoms(fromAccount)
                val resp = StringBuilder()
                resp.append("**Множители**:\n")
                for (fandom in fandoms) {
                    resp.append("@fandom_${fandom.fandomId}: ")
                    resp.append("x${ToolsText.numToStringRound(fandom.multiplier, 1)}")
                    resp.append('\n')
                }
                if (fandoms.isEmpty()) {
                    resp.append("Вы не подписаны ни на один фэндом, либо все множители нулевые")
                }
                respondTo(fromAccount, resp.toString())
            }
            "фандомы" -> {
                respondTo(fromAccount, "Не фандомы, а фэндомы. ;) Как минимум в Bonfire")
            }
            else -> {
                respondTo(fromAccount, "**Помощь**:\nfandoms - показать возможные фэндомы для ежедневного задания")
            }
        }
    }
}

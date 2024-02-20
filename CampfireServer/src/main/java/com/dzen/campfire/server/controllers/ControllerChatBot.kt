package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.executors.chat.EChatMessageCreate
import com.dzen.campfire.server.rust.RustDailyTask
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsText
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

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

    sealed interface UserState
    data class BugReportState(
        val authorId: Long,
        val authorName: String,
    ) : UserState

    private val userStates = ConcurrentHashMap<Long, UserState>()

    private fun sendBug(state: BugReportState, text: String) {
        val req = Json()
        req.put("contactEmail", "users+${state.authorName}@users.bonfire.moe")
        req.put("contactName", state.authorName)
        req.put("origin", "INAPP")
        req.put("painLevel", "UNKNOWN")
        req.put("text", text)

        val reqBytes = req.toBytes()

        val conn = URL("https://productlane.com/api/v1/insights").openConnection()
                as HttpURLConnection
        conn.setRequestProperty("Authorization", "Bearer ${App.secretsKeys.getString("productlane_key")}")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("Content-Length", reqBytes.size.toString())
        conn.requestMethod = "POST"
        conn.doOutput = true

        val outStream = conn.outputStream
        outStream.write(reqBytes)

        val code = conn.responseCode
        if (code != 200) {
            throw RuntimeException("Code not 200, but $code")
        }

        conn.inputStream.close()
    }

    fun handleMessage(fromAccount: Long, text: String) {
        val lowerText = text.lowercase().trim('.')

        val state = userStates[fromAccount]
        if (state is BugReportState) {
            if (lowerText == "cancel" || lowerText == "отмена") {
                userStates.remove(fromAccount)
                respondTo(fromAccount, "Действие отменено.")
                return
            }

            try {
                sendBug(state, text)
                respondTo(fromAccount, "Ваш запрос был успешно отправлен.\n" +
                        "Где-то ошиблись? Не стесняйтесь отправить второй запрос!")
            } catch (e: Exception) {
                err(e)
                respondTo(fromAccount, "Произошла неизвестная ошибка и ваш запрос не " +
                        "был отправлен. :( Напишите @sit'у.")
            }

            userStates.remove(fromAccount)

            return
        }

        when (lowerText) {
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
            "фандомы", "фендомы" -> {
                respondTo(fromAccount, "Не $lowerText, а фэндомы. ;) Как минимум в Bonfire")
            }
            "bug", "баг", "фича", "feature", "запрос", "поддержка", "support", "request" -> {
                val name = ControllerAccounts.getAccount(fromAccount)?.name ?: "<???>"

                userStates[fromAccount] = BugReportState(fromAccount, name)
                respondTo(fromAccount, "Опишите свои пожелания, баг или любой другой запрос в " +
                        "свободной форме. Картинки можно отправлять *только* по ссылке.\n\nУчтите, " +
                        "что изменённые сообщения не учитываются. Если вы ошиблись (или передумали), " +
                        "напишите `отмена`.")
            }
            else -> {
                respondTo(
                    fromAccount,
                    """
                        **Помощь**:
                        fandoms - показать возможные фэндомы для ежедневного задания
                        request - отправить пожелания, баг, фичу и любой другой запрос
                    """.trimIndent()
                )
            }
        }
    }
}

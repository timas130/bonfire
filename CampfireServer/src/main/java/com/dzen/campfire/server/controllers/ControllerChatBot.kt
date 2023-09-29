package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.executors.chat.EChatMessageCreate
import com.dzen.campfire.server.rust.RustDailyTask
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
        val title: String? = null,
        val description: String? = null,
    ) : UserState

    private val userStates = ConcurrentHashMap<Long, UserState>()

    private fun sendBug(state: BugReportState) {
        val req = Json()
        req.put("query", """
            mutation IssueCreate(${"$"}input: IssueCreateInput!) {
                issueCreate(input: ${"$"}input) {
                    success
                }
            }
        """.trimIndent())
        req.put("variables", Json().apply {
            put("input", Json().apply {
                put("title", state.title)
                put("description", "Автор: ID ${state.authorId}\n\n ${state.description}")
                put("teamId", App.secretsKeys.getString("linear_team"))
            })
        })

        val reqBytes = req.toBytes()

        val conn = URL("https://api.linear.app/graphql").openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", App.secretsKeys.getString("linear_key"))
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

        val resp = Json(conn.inputStream.use { it.readBytes() })
        val success = resp.getJson("data")!!.getJson("issueCreate")!!.getBoolean("success")
        if (!success) {
            throw RuntimeException("Failure")
        }
    }

    fun handleMessage(fromAccount: Long, text: String) {
        val lowerText = text.lowercase()

        val state = userStates[fromAccount]
        if (state is BugReportState) {
            if (lowerText == "cancel" || lowerText == "отмена") {
                userStates.remove(fromAccount)
                respondTo(fromAccount, "Действие отменено.")
                return
            }

            if (state.title.isNullOrBlank()) {
                if (text.length > 150) {
                    respondTo(fromAccount, "Слишком длинный заголовок. Не больше 150 символов.")
                    return
                }

                userStates[fromAccount] = state.copy(title = text)
                respondTo(
                    fromAccount,
                    "Отлично. Теперь опишите баг более подробно. Не больше 1000 символов. " +
                    "Учтите, что поддерживается только текст. Изображения можно отправить только по " +
                    "ссылке."
                )
            } else if (state.description.isNullOrBlank()) {
                if (text.length > 1000) {
                    respondTo(fromAccount, "Слишком длинное описание. Не больше 1000 символов.")
                    return
                }

                userStates[fromAccount] = state.copy(description = text)
                respondTo(
                    fromAccount,
                    "Сохранено. Убедитесь, что вы всё понятно и правильно описали и напишите " +
                    "`отправить` или `send`, чтобы отослать запрос. Учтите, что изменённые сообщения " +
                    "читаются без изменений. Напишите `cancel`, чтобы начать сначала."
                )
            } else if (lowerText == "send" || lowerText == "отправить") {
                respondTo(fromAccount, "Отправка...")
                try {
                    sendBug(state)
                    respondTo(fromAccount, "Репорт отправлен. Спасибо!")
                } catch (e: Exception) {
                    e.printStackTrace()
                    respondTo(fromAccount, "Не удалось отправить репорт. Попробуйте ещё раз :(")
                } finally {
                    userStates.remove(fromAccount)
                }
            } else {
                respondTo(fromAccount, "Напишите `отправить` или `send` для отправки или `cancel` для отмены.")
            }

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
            "bug", "баг", "фича", "feature" -> {
                val name = ControllerAccounts.getAccount(fromAccount)?.name ?: "<???>"

                userStates[fromAccount] = BugReportState(fromAccount, name)
                respondTo(fromAccount, "Вы начали отправку баг репорта / запроса фичи.")
                respondTo(fromAccount, "Чтобы отменить, напишите `cancel` или `отмена`.")
                respondTo(
                    fromAccount,
                    "Для начала, кратко опишите суть репорта. Для багов: кратко опишите проблему. " +
                    "Для фич: кратко опишите нужный функционал. Не больше 150 символов."
                )
            }
            else -> {
                respondTo(
                    fromAccount,
                    """
                        **Помощь**:
                        fandoms - показать возможные фэндомы для ежедневного задания
                        bug - отправить отчёт об ошибке или запросить фичу                        
                    """.trimIndent()
                )
            }
        }
    }
}

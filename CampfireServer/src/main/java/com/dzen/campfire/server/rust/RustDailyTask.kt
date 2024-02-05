package com.dzen.campfire.server.rust

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.daily_tasks.DailyTask
import com.dzen.campfire.api.models.daily_tasks.DailyTaskInfo
import com.dzen.campfire.api.models.daily_tasks.DailyTaskType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object RustDailyTask {
    @Serializable
    data class DailyTaskFandom(
        val fandomId: Long,
        val multiplier: Float,
    )

    @Serializable
    private data class DailyTaskFandomsResponse(
        val userById: DailyTaskFandomsUserById?,
    )

    @Serializable
    private data class DailyTaskFandomsUserById(
        val dailyTaskFandoms: List<DailyTaskFandom>,
    )

    fun getPossibleFandoms(accountId: Long): List<DailyTaskFandom> {
        val resp = ControllerRust.queryService<DailyTaskFandomsResponse>(
            """
                query DailyTaskFandoms(${'$'}id: ID!) {
                    userById(id: ${'$'}id) {
                        dailyTaskFandoms {
                            fandomId
                            multiplier
                        }
                    }
                }
            """.trimIndent(),
            buildJsonObject {
                put("id", accountId.toString())
            }
        )
        return resp.userById!!.dailyTaskFandoms
    }

    fun getInfo(accountId: Long): DailyTaskInfo {
        val resp = ControllerRust.queryService<JsonObject>(
            """
                query DailyTask(${'$'}userId: ID!) {
                    userById(id: ${'$'}userId) {
                        dailyTask {
                            task {
                                __typename
                                amount
                                ... on PostInFandomTask {
                                    fandomId
                                }
                                ... on CommentInFandomTask {
                                    fandomId
                                }
                                ... on AnswerNewbieCommentTask {
                                    maxLevel
                                }
                                ... on CommentNewbiePostTask {
                                    maxLevel
                                }
                                ... on CreatePostWithPageTypeTask {
                                    pageType
                                }
                            }
                            progress
                            totalLevels
                            levelMultiplier
                            comboMultiplier
                            possibleReward
                            fandomName
                        }
                    }
                }
            """.trimIndent(),
            buildJsonObject {
                put("userId", accountId.toString())
            }
        )

        val dailyTaskInfo = resp["userById"]!!.jsonObject["dailyTask"]!!.jsonObject

        val task = dailyTaskInfo["task"]!!.jsonObject.let {
            val type = DailyTaskType.valueOf(it["__typename"]!!.jsonPrimitive.content.removeSuffix("Task"))
            val pageType = when (it["pageType"]?.jsonPrimitive?.content) {
                "TEXT" -> API.PAGE_TYPE_TEXT
                "IMAGE" -> API.PAGE_TYPE_IMAGE
                "IMAGES" -> API.PAGE_TYPE_IMAGES
                "LINK" -> API.PAGE_TYPE_LINK
                "QUOTE" -> API.PAGE_TYPE_QUOTE
                "SPOILER" -> API.PAGE_TYPE_SPOILER
                "POLLING" -> API.PAGE_TYPE_POLLING
                "VIDEO" -> API.PAGE_TYPE_VIDEO
                "TABLE" -> API.PAGE_TYPE_TABLE
                "DOWNLOAD" -> API.PAGE_TYPE_DOWNLOAD
                "CAMPFIRE_OBJECT" -> API.PAGE_TYPE_CAMPFIRE_OBJECT
                "USER_ACTIVITY" -> API.PAGE_TYPE_USER_ACTIVITY
                "LINK_IMAGE" -> API.PAGE_TYPE_LINK_IMAGE
                "CODE" -> API.PAGE_TYPE_CODE
                else -> null
            }
            val amount = it["amount"]!!.jsonPrimitive.long
            val fandomId = it["fandomId"]?.jsonPrimitive?.long
            val maxLevel = it["maxLevel"]?.jsonPrimitive?.long

            DailyTask().apply {
                this.type = type
                this.pageType = pageType ?: 0L
                this.amount = amount
                this.fandomId = fandomId ?: 0L
                this.maxLevel = maxLevel ?: 0L
            }
        }

        return DailyTaskInfo().apply {
            this.task = task
            this.total = task.amount
            this.progress = dailyTaskInfo["progress"]!!.jsonPrimitive.long
            this.totalLevels = dailyTaskInfo["totalLevels"]!!.jsonPrimitive.long
            this.levelMultiplier = dailyTaskInfo["levelMultiplier"]!!.jsonPrimitive.float
            this.comboMultiplier = dailyTaskInfo["comboMultiplier"]!!.jsonPrimitive.float
            this.possibleReward = dailyTaskInfo["possibleReward"]!!.jsonPrimitive.long
            this.fandomName = dailyTaskInfo["fandomName"]!!.jsonPrimitive.contentOrNull
        }
    }

    fun checkIn(accountId: Long) {
        ControllerRust.queryService<JsonObject>(
            """
                mutation CheckIn(${'$'}userId: ID!) {
                    internalCheckIn(userId: ${'$'}userId)
                }
            """.trimIndent(),
            buildJsonObject {
                put("userId", accountId.toString())
            }
        )
    }
}

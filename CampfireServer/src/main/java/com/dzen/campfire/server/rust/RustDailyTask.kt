package com.dzen.campfire.server.rust

import com.dzen.campfire.api.models.daily_tasks.DailyTaskInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object RustDailyTask {
    @Serializable
    data class DailyTaskFandom(
        @SerialName("fandom_id")
        val fandomId: Long,
        val multiplier: Float,
    )

    fun getPossibleFandoms(accountId: Long): List<DailyTaskFandom> {
        val resp = ControllerRust.getBytes("/user/$accountId/dt/fandoms")
        return Json.decodeFromString(String(resp))
    }

    fun getInfo(accountId: Long): DailyTaskInfo {
        val resp = ControllerRust.get("/user/$accountId/dt/task")
        return DailyTaskInfo().apply { json(false, resp) }
    }
}

package com.dzen.campfire.server.rust

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object RustAchievements {
    @Serializable
    data class LevelRecountReport(
        @SerialName("user_id")
        var userId: Long,
        @SerialName("total_level")
        var totalLevel: Long,
        var achievements: HashMap<Long, AchievementRecountReport>,
    )

    @Serializable
    data class AchievementRecountReport(
        val id: Long,
        val count: Long,
        val target: Long?,
        val level: Long,
    )

    fun getForUser(userId: Long): LevelRecountReport {
        return Json.decodeFromString(String(ControllerRust.getBytes("/user/$userId/recount-level")))
    }
}

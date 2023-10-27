package com.dzen.campfire.server.rust

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object RustAchievements {
    @Serializable
    data class UserWithId(
        val id: Long,
    )

    @Serializable
    data class LevelRecountReport(
        var user: UserWithId,
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

    @Serializable
    private data class InternalRecountResponse(
        val internalRecountLevel: LevelRecountReport,
    )

    fun getForUser(userId: Long): LevelRecountReport {
        val resp = ControllerRust.queryService<InternalRecountResponse>(
            """
                query InternalRecount(${'$'}userId: Int!) {
                    internalRecountLevel(userId: ${'$'}userId) {
                        totalLevel
                        achievements
                        user {
                            id
                        }
                    }
                }
            """.trimIndent(),
            buildJsonObject {
                put("userId", userId)
            }
        )
        return resp.internalRecountLevel
    }
}

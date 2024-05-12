package com.dzen.campfire.server.rust

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.BufferedSinkJsonWriter
import com.apollographql.apollo3.api.obj
import com.dzen.campfire.server.InternalRecountQuery
import com.dzen.campfire.server.fragment.LevelRecountReport
import com.dzen.campfire.server.fragment.LevelRecountReportImpl_ResponseAdapter
import com.dzen.campfire.server.rust.ControllerRust.executeExt
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.Buffer
import sh.sit.bonfire.schema.jsonReader

object RustAchievements {
    @Serializable
    data class AchievementRecountReport(
        val id: Long,
        val count: Long,
        val target: Long?,
        val level: Long,
    )

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun LevelRecountReport.achievements(): HashMap<Long, AchievementRecountReport> {
        return json.decodeFromString(achievements)
    }

    //#region crimes against humanity
    fun LevelRecountReport.serialize(): String {
        val buffer = Buffer()
        BufferedSinkJsonWriter(buffer).apply {
            LevelRecountReportImpl_ResponseAdapter.LevelRecountReport.obj()
                .toJson(this, CustomScalarAdapters.Empty, this@serialize)
        }
        return buffer.readUtf8()
    }
    fun deserializeLevelRecountReport(json: String): LevelRecountReport {
        return jsonReader(json).use {
            LevelRecountReportImpl_ResponseAdapter.LevelRecountReport.obj()
                .fromJson(it, CustomScalarAdapters.Empty)
        }
    }
    //#endregion crimes against humanity

    fun getForUser(userId: Long): LevelRecountReport {
        val result = ControllerRust.apollo.query(InternalRecountQuery(userId.toInt()))
            .executeExt()
        return result.internalRecountLevel.levelRecountReport
    }
}

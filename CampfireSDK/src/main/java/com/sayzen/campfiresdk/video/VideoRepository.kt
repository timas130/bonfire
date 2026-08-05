package com.sayzen.campfiresdk.video

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.exception.ApolloException
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.VideoByIdQuery
import com.sayzen.campfiresdk.VideoUploadLimitsQuery
import com.sayzen.campfiresdk.fragment.VideoState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import sh.sit.bonfire.auth.ApolloController
import sh.sit.schema.type.VideoStatus
import kotlin.time.Duration.Companion.seconds

object VideoRepository {
    data class UploadLimits(
        val maxSizeBytes: Long,
        val maxDurationMs: Long,
        val dailyLimit: Int,
    )

    @Volatile
    private var cachedLimits: UploadLimits? = null

    val VideoStatus.isTerminal: Boolean
        get() = this == VideoStatus.READY || this == VideoStatus.FAILED || this == VideoStatus.DELETED

    suspend fun getVideo(videoId: String): VideoState? {
        val response = ApolloController.apolloClient
            .query(VideoByIdQuery(videoId))
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .execute()
        return response.dataAssertNoErrors.videoById?.videoState
    }

    fun pollVideo(videoId: String): Flow<VideoState?> = flow {
        val startedAt = System.currentTimeMillis()
        var attempt = 0
        while (true) {
            val video = getVideo(videoId)
            emit(video)
            if (video == null || video.status.isTerminal) return@flow
            if (System.currentTimeMillis() - startedAt > 5 * 60_000) return@flow
            delay(2.seconds)
            attempt++
        }
    }.retryWhen { cause, attempt ->
        // note that `attempt` != `var attempt` above
        (cause is ApolloException && attempt < 5).also {
            if (it) delay(5.seconds)
        }
    }

    suspend fun getUploadLimits(): UploadLimits {
        cachedLimits?.let { return it }
        return try {
            val limits = ApolloController.apolloClient
                .query(VideoUploadLimitsQuery())
                .fetchPolicy(FetchPolicy.NetworkOnly)
                .execute()
                .dataAssertNoErrors
                .videoUploadLimits
            UploadLimits(
                maxSizeBytes = limits.maxSizeBytes.toLong(),
                maxDurationMs = limits.maxDurationMs.toLong(),
                dailyLimit = limits.dailyLimit,
            ).also { cachedLimits = it }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()

            UploadLimits(
                maxSizeBytes = API.PAGE_VIDEO_LOCAL_MAX_SIZE_BYTES,
                maxDurationMs = API.PAGE_VIDEO_LOCAL_MAX_DURATION_MS,
                dailyLimit = 0,
            )
        }
    }
}

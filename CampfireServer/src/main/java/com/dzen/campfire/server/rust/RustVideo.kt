package com.dzen.campfire.server.rust

import com.apollographql.apollo3.exception.ApolloException
import com.dzen.campfire.api.API
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.InternalVideoDuplicateMutation
import com.dzen.campfire.server.InternalVideoReleaseMutation
import com.dzen.campfire.server.InternalVideoSetPublicationMutation
import com.dzen.campfire.server.VideosByIdsQuery
import com.dzen.campfire.server.rust.ControllerRust.executeExt
import com.sup.dev.java.libs.debug.err
import sh.sit.schema.type.VideoStatus

object RustVideo {
    val VIDEO_ID_REGEX = Regex("^[A-Za-z0-9_-]{10,32}$")

    class Video(
        val id: String,
        val userId: Long,
        val publicationId: Long?,
        val status: VideoStatus,
        val durationMs: Long?,
        val width: Int?,
        val height: Int?,
    )

    private fun <T> callVideoService(block: () -> T): T {
        try {
            return block()
        } catch (e: ApiException) {
            throw e
        } catch (e: ApolloException) {
            err(e)
            throw ApiException(API.ERROR_VIDEO_SERVICE_UNAVAILABLE)
        }
    }

    fun getByIds(ids: List<String>): List<Video> = callVideoService {
        ControllerRust.apollo.query(VideosByIdsQuery(ids))
            .executeExt()
            .videosByIds
            .map { raw ->
                Video(
                    id = raw.id,
                    userId = raw.creatorId.toLong(),
                    publicationId = raw.publicationId?.toLong(),
                    status = raw.status,
                    durationMs = raw.durationMs?.toLong(),
                    width = raw.width,
                    height = raw.height,
                )
            }
    }

    fun getById(id: String): Video? = getByIds(listOf(id)).firstOrNull()

    fun setPublication(videoId: String, publicationId: Long) {
        callVideoService {
            ControllerRust.apollo.mutation(InternalVideoSetPublicationMutation(videoId, publicationId.toString()))
                .executeExt()
        }
    }

    fun release(videoId: String) {
        callVideoService {
            ControllerRust.apollo.mutation(InternalVideoReleaseMutation(videoId))
                .executeExt()
        }
    }

    fun duplicate(videoId: String, publicationId: Long): String = callVideoService {
        ControllerRust.apollo.mutation(InternalVideoDuplicateMutation(videoId, publicationId.toString()))
            .executeExt()
            .internalVideoDuplicate
            .id
    }
}

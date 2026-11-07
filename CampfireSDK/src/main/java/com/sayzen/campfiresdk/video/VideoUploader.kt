package com.sayzen.campfiresdk.video

import android.content.ContentResolver
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.apollographql.apollo3.api.Error
import com.apollographql.apollo3.exception.ApolloException
import com.dzen.campfire.api.models.publications.post.PageVideo
import com.sayzen.campfiresdk.VideoUploadCancelMutation
import com.sayzen.campfiresdk.VideoUploadFinishMutation
import com.sayzen.campfiresdk.VideoUploadStartMutation
import com.sayzen.campfiresdk.controllers.ControllerVideos
import com.sup.dev.android.utils.UtilsMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.BufferedSink
import sh.sit.bonfire.auth.ApolloController
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object VideoUploader {
    class VideoUploadException(
        val reason: Reason,
        cause: Throwable? = null,
    ) : Exception(reason.name, cause) {
        enum class Reason {
            NotVideo,
            TooLong,
            TooLarge,
            UnknownSize,
            UnknownDuration,
            LevelTooLow,
            DailyLimit,
            Network,
            Unknown,
        }
    }

    data class VideoPrecheck(
        val sizeBytes: Long,
        val durationMs: Long,
        val width: Int,
        val height: Int,
        val limits: VideoRepository.UploadLimits,
    )

    suspend fun precheck(context: Context, uri: Uri): VideoPrecheck = withContext(Dispatchers.IO) {
        val limits = VideoRepository.getUploadLimits()

        val sizeBytes = resolveSize(context.contentResolver, uri)
            ?: throw VideoUploadException(VideoUploadException.Reason.UnknownSize)

        val retriever = MediaMetadataRetriever()
        val metadata = try {
            retriever.setDataSource(context, uri)
            val utils = UtilsMetadata(retriever)
            if (!utils.hasVideo()) throw VideoUploadException(VideoUploadException.Reason.NotVideo)

            // I hope this BS never gets used and I just wasted my time
            val rotation = try {
                utils.getVideoRotation()
            } catch (_: Exception) {
                0
            }
            val rotated = rotation == 90 || rotation == 270
            Triple(
                utils.getDurationMs().toLong(),
                if (rotated) utils.getVideoHeight() else utils.getVideoWidth(),
                if (rotated) utils.getVideoWidth() else utils.getVideoHeight(),
            )
        } catch (e: VideoUploadException) {
            throw e
        } catch (e: Exception) {
            throw VideoUploadException(VideoUploadException.Reason.NotVideo, e)
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val (durationMs, width, height) = metadata

        if (durationMs <= 0) {
            throw VideoUploadException(VideoUploadException.Reason.UnknownDuration)
        }
        if (durationMs > limits.maxDurationMs) {
            throw VideoUploadException(VideoUploadException.Reason.TooLong)
        }
        if (sizeBytes > limits.maxSizeBytes) {
            throw VideoUploadException(VideoUploadException.Reason.TooLarge)
        }

        VideoPrecheck(
            sizeBytes = sizeBytes,
            durationMs = durationMs,
            width = width,
            height = height,
            limits = limits,
        )
    }

    // videoUploadStart + s3 upload + videoUploadFinish
    suspend fun upload(
        context: Context,
        uri: Uri,
        precheck: VideoPrecheck,
        onProgress: (sentBytes: Long) -> Unit,
    ): PageVideo {
        val ticket = startUpload(precheck.sizeBytes)
        try {
            putToS3(context.contentResolver, uri, ticket.uploadUrl, precheck.sizeBytes, onProgress)
            finishUpload(ticket.id)
        } catch (e: Throwable) {
            withContext(NonCancellable) {
                try {
                    ApolloController.apolloClient
                        .mutation(VideoUploadCancelMutation(ticket.id))
                        .execute()
                } catch (cancelError: Exception) {
                    cancelError.printStackTrace()
                }
            }
            throw e
        }

        return PageVideo().apply {
            videoId = ticket.id
            w = precheck.width
            h = precheck.height
            durationMs = precheck.durationMs
        }
    }

    private suspend fun startUpload(sizeBytes: Long): VideoUploadStartMutation.VideoUploadStart {
        val response = try {
            ApolloController.apolloClient
                .mutation(VideoUploadStartMutation(sizeBytes.toInt()))
                .execute()
        } catch (e: ApolloException) {
            throw VideoUploadException(VideoUploadException.Reason.Network, e)
        }
        if (response.hasErrors()) {
            throw uploadExceptionFromError(response.errors!!.first())
        }
        return response.dataAssertNoErrors.videoUploadStart
    }

    private suspend fun finishUpload(videoId: String) {
        val response = try {
            ApolloController.apolloClient
                .mutation(VideoUploadFinishMutation(videoId))
                .execute()
        } catch (e: ApolloException) {
            throw VideoUploadException(VideoUploadException.Reason.Network, e)
        }
        if (response.hasErrors()) {
            throw uploadExceptionFromError(response.errors!!.first())
        }
    }

    private fun uploadExceptionFromError(error: Error): VideoUploadException {
        val reason = when (error.message.substringBefore(':')) {
            "LevelTooLow" -> VideoUploadException.Reason.LevelTooLow
            "DailyLimit", "DailyLimitReached" -> VideoUploadException.Reason.DailyLimit
            "TooLarge" -> VideoUploadException.Reason.TooLarge
            "TooLong" -> VideoUploadException.Reason.TooLong
            else -> VideoUploadException.Reason.Unknown
        }
        return VideoUploadException(reason)
    }

    // this is umazing
    private fun resolveSize(resolver: ContentResolver, uri: Uri): Long? {
        try {
            resolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                if (afd.length >= 0) return afd.length
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && cursor.moveToFirst() && !cursor.isNull(sizeIndex)) {
                    return cursor.getLong(sizeIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private suspend fun putToS3(
        resolver: ContentResolver,
        uri: Uri,
        uploadUrl: String,
        sizeBytes: Long,
        onProgress: (sentBytes: Long) -> Unit,
    ) = suspendCancellableCoroutine { continuation ->
        val request = Request.Builder()
            .url(uploadUrl)
            .put(ContentUriRequestBody(resolver, uri, sizeBytes, onProgress))
            .build()
        val call = ControllerVideos.uploadClient.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) {
                    continuation.resumeWithException(
                        VideoUploadException(VideoUploadException.Reason.Network, e)
                    )
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!continuation.isActive) return
                    if (it.isSuccessful) {
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(
                            VideoUploadException(
                                VideoUploadException.Reason.Network,
                                IOException("S3 PUT failed: HTTP ${it.code}")
                            )
                        )
                    }
                }
            }
        })
    }

    private class ContentUriRequestBody(
        private val resolver: ContentResolver,
        private val uri: Uri,
        private val length: Long,
        private val onProgress: (sentBytes: Long) -> Unit,
    ) : RequestBody() {
        override fun contentType(): MediaType? =
            (resolver.getType(uri) ?: "application/octet-stream").toMediaTypeOrNull()

        override fun contentLength(): Long = length

        // one opportunity...
        override fun isOneShot(): Boolean = true

        override fun writeTo(sink: BufferedSink) {
            val input = resolver.openInputStream(uri)
                ?: throw IOException("cannot open input stream for $uri")
            input.use { stream ->
                val buffer = ByteArray(BUFFER_SIZE)
                var sent = 0L
                var lastProgressAt = 0L
                while (true) {
                    val read = stream.read(buffer)
                    if (read == -1) break
                    sink.write(buffer, 0, read)
                    sent += read
                    val now = System.currentTimeMillis()
                    if (now - lastProgressAt >= PROGRESS_INTERVAL_MS || sent >= length) {
                        lastProgressAt = now
                        onProgress(sent)
                    }
                }
            }
        }

        companion object {
            private const val BUFFER_SIZE = 64 * 1024
            private const val PROGRESS_INTERVAL_MS = 100L
        }
    }
}

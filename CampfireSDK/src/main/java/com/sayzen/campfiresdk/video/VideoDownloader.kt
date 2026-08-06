package com.sayzen.campfiresdk.video

import android.Manifest
import android.os.Build
import com.sayzen.campfiresdk.controllers.ControllerVideos
import com.sup.dev.android.tools.ToolsPermission
import com.sup.dev.android.tools.ToolsStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import sh.sit.schema.type.VideoStatus
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object VideoDownloader {
    private const val MIME = "video/mp4"
    private const val EXTENSION = "mp4"
    private const val BUFFER_SIZE = 64 * 1024

    class NotDownloadableException : Exception("the video is not ready")
    class PermissionException : Exception("storage permission was denied")

    /**
     * Saves the encoded MP4 into the shared Movies collection.
     *
     * The URL is resolved right here rather than reused from the player: the
     * presigned link expires, and a player left open for an hour would hand us
     * a dead one.
     */
    suspend fun download(videoId: String) {
        val video = VideoRepository.getVideo(videoId)
        val url = video?.url?.takeIf { video.status == VideoStatus.READY }
            ?: throw NotDownloadableException()

        // scoped storage covers Q and up; below that we write the file ourselves
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) requestWritePermission()

        val response = get(url)
        withContext(Dispatchers.IO) {
            response.use { r ->
                ToolsStorage.saveToMovies(MIME, EXTENSION) { out ->
                    val input = r.body.byteStream()
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        // copyTo would not notice the screen being closed
                        ensureActive()
                        val read = input.read(buffer)
                        if (read == -1) break
                        out.write(buffer, 0, read)
                    }
                }
            }
        }
    }

    private suspend fun get(url: String): Response = suspendCancellableCoroutine { continuation ->
        val call = ControllerVideos.client.newCall(Request.Builder().url(url).build())
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!continuation.isActive) {
                    response.close()
                    return
                }
                if (response.isSuccessful) {
                    continuation.resume(response)
                } else {
                    response.close()
                    continuation.resumeWithException(IOException("download failed: HTTP ${response.code}"))
                }
            }
        })
    }

    private suspend fun requestWritePermission() = suspendCancellableCoroutine { continuation ->
        ToolsPermission.requestPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            onGranted = { if (continuation.isActive) continuation.resume(Unit) },
            onPermissionRestriction = {
                if (continuation.isActive) continuation.resumeWithException(PermissionException())
            },
        )
    }
}

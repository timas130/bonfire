package com.sayzen.campfiresdk.video.player

import android.content.Context
import androidx.compose.runtime.*
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerVideos
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.video.VideoDownloader
import com.sayzen.campfiresdk.video.VideoRepository
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.java.libs.debug.err
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sh.sit.schema.type.VideoStatus

@androidx.annotation.OptIn(UnstableApi::class)
@Immutable
class VideoPlayerModel(
    private val context: Context,
    private val videoId: String,
    private val scope: CoroutineScope,
) {
    var player by mutableStateOf<ExoPlayer?>(null)
        private set
    var error by mutableStateOf(false)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var isBuffering by mutableStateOf(true)
        private set
    var hasEnded by mutableStateOf(false)
        private set

    var released by mutableStateOf(false)
        private set

    var playbackSpeed by mutableFloatStateOf(1f)
        private set

    var downloading by mutableStateOf(false)
        private set

    var position by mutableLongStateOf(0L)
    var duration by mutableLongStateOf(0L)

    private var playWhenReady = true

    private var reresolvedOnExpiry = false
    private var loadJob: Job? = null
    private var downloadJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            isPlaying = playing
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            this@VideoPlayerModel.playWhenReady = playWhenReady
        }

        override fun onPlaybackStateChanged(state: Int) {
            isBuffering = state == Player.STATE_BUFFERING
            hasEnded = state == Player.STATE_ENDED
            val d = player?.duration ?: 0
            if (d > 0) duration = d
            // the position ticker stops with playback, so it would otherwise
            // freeze a few frames short of the end
            if (hasEnded && duration > 0) position = duration
        }

        override fun onPlayerError(playbackError: PlaybackException) {
            if (isExpiredUrlError(playbackError) && !reresolvedOnExpiry) {
                reresolvedOnExpiry = true
                resolveAndPrepare(resumeMs = position)
            } else {
                error = true
            }
        }
    }

    fun load() {
        resolveAndPrepare(resumeMs = if (position > 0) position else 0)
    }

    private fun resolveAndPrepare(resumeMs: Long) {
        loadJob?.cancel()
        loadJob = scope.launch {
            error = false
            hasEnded = false
            val video = try {
                VideoRepository.getVideo(videoId)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            val url = video?.url?.takeIf { video.status == VideoStatus.READY }
            if (url == null) {
                error = true
                return@launch
            }

            val p = player ?: ExoPlayer.Builder(context)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(OkHttpDataSource.Factory(ControllerVideos.client))
                )
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .build(),
                    true
                )
                .setHandleAudioBecomingNoisy(true)
                .build()
                .apply { addListener(listener) }
                .also { player = it }

            p.setMediaItem(MediaItem.fromUri(url))
            p.prepare()
            if (resumeMs > 0) p.seekTo(resumeMs)
            p.setPlaybackSpeed(playbackSpeed)
            p.playWhenReady = playWhenReady
        }
    }

    val wantsToPlay: Boolean
        get() = playWhenReady

    fun play() {
        playWhenReady = true
        player?.playWhenReady = true
    }

    fun pause() {
        playWhenReady = false
        player?.playWhenReady = false
    }

    /**
     * A finished player still reports `playWhenReady = true`, so plain [play]
     * would leave it parked on the last frame. Rewind first.
     */
    fun restart() {
        seekTo(0)
        play()
    }

    fun togglePlayPause() {
        when {
            hasEnded -> restart()
            player?.isPlaying == true -> pause()
            else -> play()
        }
    }

    fun selectPlaybackSpeed(speed: Float) {
        playbackSpeed = speed
        player?.setPlaybackSpeed(speed)
    }

    fun overridePlaybackSpeed(speed: Float) {
        player?.setPlaybackSpeed(speed)
    }

    fun restorePlaybackSpeed() {
        player?.setPlaybackSpeed(playbackSpeed)
    }

    fun seekTo(ms: Long) {
        val p = player ?: return
        val total = p.duration
        p.seekTo(ms.coerceIn(0L, if (total > 0) total else Long.MAX_VALUE))
        position = p.currentPosition
    }

    fun seekBy(deltaMs: Long) {
        val p = player ?: return
        seekTo(p.currentPosition + deltaMs)
    }

    fun syncPositionFromPlayer() {
        val p = player ?: return
        val newPosition = p.currentPosition
        if (newPosition != position) position = newPosition
        val d = p.duration
        if (d > 0 && d != duration) duration = d
    }

    fun download() {
        if (downloadJob?.isActive == true) return
        downloadJob = scope.launch {
            downloading = true
            ToolsToast.show(t(API_TRANSLATE.app_downloading))
            try {
                VideoDownloader.download(videoId)
                ToolsToast.show(t(API_TRANSLATE.app_downloaded))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                err(e)
                ToolsToast.show(
                    if (e is VideoDownloader.PermissionException) SupAndroid.TEXT_ERROR_PERMISSION_FILES
                    else ToolsResources.s(R.string.video_download_failed)
                )
            } finally {
                downloading = false
            }
        }
    }

    fun release() {
        if (released) return
        released = true
        loadJob?.cancel()
        downloadJob?.cancel()
        player?.removeListener(listener)
        player?.release()
        player = null
    }

    private fun isExpiredUrlError(playbackError: PlaybackException): Boolean {
        var cause: Throwable? = playbackError
        while (cause != null) {
            if (cause is HttpDataSource.InvalidResponseCodeException && cause.responseCode == 403) {
                return true
            }
            cause = cause.cause
        }
        return false
    }
}

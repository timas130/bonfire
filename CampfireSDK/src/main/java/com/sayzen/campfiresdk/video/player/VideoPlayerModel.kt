package com.sayzen.campfiresdk.video.player

import android.content.Context
import androidx.compose.runtime.*
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.sayzen.campfiresdk.controllers.ControllerVideos
import com.sayzen.campfiresdk.video.VideoRepository
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

    var released by mutableStateOf(false)
        private set

    var playbackSpeed by mutableFloatStateOf(1f)
        private set

    var position by mutableLongStateOf(0L)
    var duration by mutableLongStateOf(0L)

    private var playWhenReady = true

    private var reresolvedOnExpiry = false
    private var loadJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            isPlaying = playing
        }

        override fun onPlaybackStateChanged(state: Int) {
            isBuffering = state == Player.STATE_BUFFERING
            val d = player?.duration ?: 0
            if (d > 0) duration = d
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

    fun togglePlayPause() {
        if (player?.isPlaying == true) pause() else play()
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

    fun release() {
        if (released) return
        released = true
        loadJob?.cancel()
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

package com.sayzen.campfiresdk.video.inline

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.sayzen.campfiresdk.controllers.ControllerVideos

class VideoAutoplayCoordinator {
    private class Candidate(
        var url: String,
        var topY: Float,
        var visible: Boolean,
    )

    private val candidates = HashMap<Long, Candidate>()
    private var nextKey = 0L

    var activeKey by mutableStateOf<Long?>(null)
        private set

    private var player: ExoPlayer? = null
    private var attachedKey: Long? = null

    fun newKey(): Long = nextKey++

    fun update(key: Long, url: String, visible: Boolean, topY: Float) {
        val candidate = candidates.getOrPut(key) { Candidate(url, topY, visible) }
        candidate.url = url
        candidate.topY = topY
        candidate.visible = visible
        recomputeActive()
    }

    fun remove(key: Long) {
        candidates.remove(key)
        if (attachedKey == key) detach()
        recomputeActive()
    }

    private fun recomputeActive() {
        val newActive = candidates.entries
            .filter { it.value.visible }
            .minByOrNull { it.value.topY }
            ?.key
        activeKey = newActive
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    fun acquirePlayer(context: Context, key: Long): ExoPlayer? {
        if (key != activeKey) return null
        val url = candidates[key]?.url ?: return null

        val p = player ?: ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(OkHttpDataSource.Factory(ControllerVideos.client)))
            .build()
            .apply {
                volume = 0f
                repeatMode = Player.REPEAT_MODE_ONE
            }
            .also { player = it }

        if (attachedKey != key) {
            attachedKey = key
            p.setMediaItem(MediaItem.fromUri(url))
            p.prepare()
        }
        p.play()
        return p
    }

    fun releaseFor(key: Long) {
        if (attachedKey == key) detach()
    }

    fun pause() {
        player?.pause()
    }

    fun resume() {
        if (attachedKey != null) player?.play()
    }

    fun release() {
        detach()
        player?.release()
        player = null
        candidates.clear()
        activeKey = null
    }

    private fun detach() {
        attachedKey = null
        player?.stop()
        player?.clearMediaItems()
    }
}

val LocalVideoAutoplayCoordinator = staticCompositionLocalOf<VideoAutoplayCoordinator?> { null }

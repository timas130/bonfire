package com.sayzen.campfiresdk.video.player

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sayzen.campfiresdk.compose.BonfireTheme
import com.sayzen.campfiresdk.compose.ComposeScreen

class SVideoPlayer(videoId: String) : ComposeScreen() {
    private val holder = VideoPlayerModel(
        context = context,
        videoId = videoId,
        scope = coroutineScope,
    )

    private var resumePlaybackOnReturn = false

    init {
        isSingleInstanceInBackStack = true
        statusBarColor = Color.BLACK
        navigationBarColor = Color.BLACK
        statusBarIsLight = false
        navigationBarIsLight = false

        holder.load()
    }

    override fun onResume() {
        super.onResume()
        setFullscreen(true)
        if (resumePlaybackOnReturn) {
            resumePlaybackOnReturn = false
            holder.play()
        }
    }

    override fun onPause() {
        super.onPause()
        resumePlaybackOnReturn = resumePlaybackOnReturn || holder.wantsToPlay
        holder.pause()
        setFullscreen(false)
    }

    /**
     * Swapping navigator stacks (the feed/chats tabs) moves this screen out of
     * view without pausing it, which would leave audio playing and the system
     * bars hidden under the other stack. Coming back re-runs [onResume].
     */
    override fun onStackChanged() {
        super.onStackChanged()
        onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        holder.release()
        setFullscreen(false)
    }

    private fun setFullscreen(enabled: Boolean) {
        val window = getActivity().window
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (enabled) {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    @Composable
    override fun Content() {
        BonfireTheme(useDarkTheme = true) {
            VideoPlayerContent(holder)
        }
    }
}

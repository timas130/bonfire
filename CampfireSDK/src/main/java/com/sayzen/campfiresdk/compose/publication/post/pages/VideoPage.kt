package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.models.publications.post.PageVideo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.util.RemoteImageShimmer
import com.sayzen.campfiresdk.controllers.ControllerVideos
import com.sayzen.campfiresdk.fragment.VideoState
import com.sayzen.campfiresdk.video.VideoRepository
import com.sayzen.campfiresdk.video.inline.LocalVideoAutoplayCoordinator
import com.sayzen.campfiresdk.video.player.SVideoPlayer
import com.sup.dev.android.libs.screens.navigator.Navigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import sh.sit.bonfire.images.RemoteImage
import sh.sit.schema.type.VideoStatus
import kotlin.time.Duration.Companion.milliseconds

private const val MIN_VISIBLE_FRACTION = 0.5f

@Composable
internal fun PageVideoRenderer(page: PageVideo) {
    var video by remember(page.videoId) { mutableStateOf<VideoState?>(null) }
    var notFound by remember(page.videoId) { mutableStateOf(false) }
    var pollFailed by remember(page.videoId) { mutableStateOf(false) }
    var retryNonce by remember(page.videoId) { mutableIntStateOf(0) }

    // cancels automatically once the page scrolls out of composition
    LaunchedEffect(page.videoId, retryNonce) {
        VideoRepository.pollVideo(page.videoId)
            .catch {
                it.printStackTrace()
                pollFailed = true
            }
            .collect { state ->
                if (state == null) notFound = true
                else video = state
            }
    }

    val width = video?.width ?: page.w
    val height = video?.height ?: page.h
    val aspectRatio = if (width > 0 && height > 0) width.toFloat() / height else 16f / 9f

    PageVideoFrame(aspectRatio = aspectRatio) {
        val status = video?.status
        when {
            notFound || status == VideoStatus.DELETED -> VideoUnavailableOverlay(
                icon = { Icon(Icons.Default.VideocamOff, contentDescription = null) },
                text = stringResource(R.string.video_deleted),
            )
            status == VideoStatus.FAILED -> VideoUnavailableOverlay(
                icon = { Icon(Icons.Default.ErrorOutline, contentDescription = null) },
                text = stringResource(R.string.video_failed),
            )
            status == VideoStatus.READY -> VideoReadyContent(page = page, video = video!!)
            pollFailed -> VideoUnavailableOverlay(
                icon = { Icon(Icons.Default.ErrorOutline, contentDescription = null) },
                text = stringResource(R.string.video_player_error),
                onRetry = {
                    pollFailed = false
                    retryNonce++
                },
            )
            status != null -> VideoProcessingOverlay()
            else -> RemoteImageShimmer()
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun BoxScope.VideoReadyContent(page: PageVideo, video: VideoState) {
    val context = LocalContext.current
    val coordinator = LocalVideoAutoplayCoordinator.current
    val autoplayEnabled = ControllerVideos.autoplayEnabled
    val url = video.url

    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var firstFrameRendered by remember { mutableStateOf(false) }

    // autoplay handing (I despise this)
    val autoplayKey = if (coordinator != null && autoplayEnabled && url != null) {
        val key = remember(coordinator) { coordinator.newKey() }
        DisposableEffect(key) {
            onDispose { coordinator.remove(key) }
        }
        key
    } else {
        null
    }

    if (coordinator != null && autoplayKey != null && url != null) {
        // amazing dance to poll the position of the video page relative to the window.
        // NOTE: I don't think onGloballyPositioned updates inside a ComposeView when it is scrolled,
        //       so we have to poll...
        val view = LocalView.current
        val windowInfo = LocalWindowInfo.current
        val coordsRef = remember { Ref<LayoutCoordinates>() }
        Box(
            Modifier
                .matchParentSize()
                .onGloballyPositioned { coordsRef.value = it }
        )

        LaunchedEffect(autoplayKey, url) {
            while (true) {
                val coords = coordsRef.value
                if (coords != null && coords.isAttached) {
                    val bounds = coords.boundsInWindow()
                    val fullHeight = coords.size.height
                    val windowHeight = windowInfo.containerSize.height.toFloat()
                    val visibleHeight = bounds.bottom.coerceIn(0f, windowHeight) -
                        bounds.top.coerceIn(0f, windowHeight)
                    val visible = view.isAttachedToWindow &&
                        fullHeight > 0 &&
                        visibleHeight >= fullHeight * MIN_VISIBLE_FRACTION
                    coordinator.update(autoplayKey, url, visible, bounds.top)
                }
                delay(500.milliseconds)
            }
        }

        val isActive = coordinator.activeKey == autoplayKey
        DisposableEffect(isActive) {
            if (isActive) {
                player = coordinator.acquirePlayer(context, autoplayKey)
            }
            onDispose {
                if (isActive) {
                    coordinator.releaseFor(autoplayKey)
                    player = null
                    firstFrameRendered = false
                }
            }
        }
    }

    player?.let { p ->
        DisposableEffect(p) {
            val listener = object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    firstFrameRendered = true
                }
            }
            p.addListener(listener)
            onDispose { p.removeListener(listener) }
        }
        PlayerSurface(
            player = p,
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            modifier = Modifier.matchParentSize(),
        )
    }
    // </autoplay handling>

    val thumbnail = video.thumbnail
    val autoplaying = player != null && firstFrameRendered
    VideoReadyOverlay(
        thumbnail = remember(thumbnail) { thumbnail?.let { ImageRef(it.i.toLong(), it.u) } },
        showThumbnail = !autoplaying,
        autoplaying = autoplaying,
        durationMs = video.durationMs?.toLong() ?: page.durationMs,
        onClick = {
            Navigator.to(SVideoPlayer(page.videoId))
        },
    )
}

@Composable
private fun PageVideoFrame(
    aspectRatio: Float,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .align(Alignment.Center)
                .heightIn(max = 700.dp)
                .aspectRatio(aspectRatio)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
            content = content,
        )
    }
}

@Composable
private fun BoxScope.VideoReadyOverlay(
    thumbnail: ImageRef?,
    showThumbnail: Boolean,
    autoplaying: Boolean,
    durationMs: Long,
    onClick: () -> Unit,
) {
    if (showThumbnail && thumbnail != null) {
        RemoteImage(
            link = thumbnail,
            contentDescription = stringResource(R.string.video_alt),
            loader = { RemoteImageShimmer() },
            contentScale = ContentScale.Crop,
            forceAspectRatio = false,
            modifier = Modifier.matchParentSize(),
        )
    }

    Box(
        Modifier
            .matchParentSize()
            .clickable(onClick = onClick)
    )

    // once it is actually playing the frame speaks for itself, so the button
    // shrinks out of the way and pairs up with the duration badge
    Icon(
        imageVector = Icons.Default.PlayArrow,
        contentDescription = stringResource(R.string.video_play),
        tint = Color.White,
        modifier = Modifier
            .align(if (autoplaying) Alignment.BottomStart else Alignment.Center)
            .padding(if (autoplaying) 8.dp else 0.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(if (autoplaying) 4.dp else 8.dp)
            .size(if (autoplaying) 20.dp else 48.dp)
    )

    if (durationMs > 0) {
        Text(
            text = formatVideoDuration(durationMs),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun BoxScope.VideoProcessingOverlay() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .align(Alignment.Center)
            .padding(12.dp)
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.video_processing),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BoxScope.VideoUnavailableOverlay(
    icon: @Composable () -> Unit,
    text: String,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .align(Alignment.Center)
            .padding(12.dp)
    ) {
        icon()
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

internal fun formatVideoDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}

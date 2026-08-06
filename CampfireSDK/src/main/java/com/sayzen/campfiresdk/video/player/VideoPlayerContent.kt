package com.sayzen.campfiresdk.video.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val SKIP_STEP_MS = 5_000L // perhaps change to 10s when/if limits are increased?
private const val FAST_FORWARD_SPEED = 2f
private const val CONTROLS_HIDE_DELAY_MS = 3500L

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
internal fun VideoPlayerContent(holder: VideoPlayerModel) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    if (holder.released) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
        return
    }

    val player = holder.player

    var controlsVisible by remember { mutableStateOf(true) }
    var scrubbing by remember { mutableStateOf(false) }
    var speedMenuOpen by remember { mutableStateOf(false) }

    // sync seek bar
    LaunchedEffect(player, holder.isPlaying, controlsVisible) {
        if (player == null) return@LaunchedEffect
        holder.syncPositionFromPlayer()
        if (!holder.isPlaying || !controlsVisible) return@LaunchedEffect
        while (true) {
            withFrameMillis { }
            holder.syncPositionFromPlayer()
        }
    }

    // autohide controls
    LaunchedEffect(controlsVisible, holder.isPlaying, scrubbing, speedMenuOpen) {
        if (controlsVisible && holder.isPlaying && !scrubbing && !speedMenuOpen) {
            delay(CONTROLS_HIDE_DELAY_MS.milliseconds)
            controlsVisible = false
        }
    }

    // a restart button nobody can see is not a restart button
    LaunchedEffect(holder.hasEnded) {
        if (holder.hasEnded) controlsVisible = true
    }

    // destroy skip ripple
    var skipFeedback by remember { mutableStateOf<SkipFeedback?>(null) }
    LaunchedEffect(skipFeedback) {
        if (skipFeedback != null) {
            delay(700.milliseconds)
            skipFeedback = null
        }
    }

    var isFastForwarding by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    var scrubValue by remember { mutableFloatStateOf(0f) }

    val ripples = remember { mutableStateListOf<TapRipple>() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val playerView = remember {
            PlayerView(context).apply {
                useController = false
                setShutterBackgroundColor(android.graphics.Color.BLACK)
            }
        }
        LaunchedEffect(player) {
            playerView.player = player
        }
        if (player != null) {
            DisposableEffect(lifecycle, playerView) {
                val observer = object : DefaultLifecycleObserver {
                    override fun onStart(owner: LifecycleOwner) {
                        playerView.onResume()
                    }

                    override fun onStop(owner: LifecycleOwner) {
                        playerView.onPause()
                    }
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }
            AndroidView(
                factory = { playerView },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (holder.error) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.video_player_error), color = Color.White)
                OutlinedButton(onClick = { holder.load() }) {
                    Text(stringResource(R.string.retry))
                }
            }
            PlayerBackButton(
                onBack = { Navigator.back() },
                modifier = Modifier
                    .padding(rememberDisplayCutoutPadding())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
            return
        }

        if (player == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
            PlayerBackButton(
                onBack = { Navigator.back() },
                modifier = Modifier
                    .padding(rememberDisplayCutoutPadding())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
            return
        }

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(holder) {
                    detectMediaGestures(
                        onSingleTap = {
                            controlsVisible = !controlsVisible
                        },
                        onMultiTap = { count, isRight, offset ->
                            holder.seekBy(if (isRight) SKIP_STEP_MS else -SKIP_STEP_MS)
                            val prev = skipFeedback
                            skipFeedback = SkipFeedback(
                                isForward = isRight,
                                seconds = (count - 1) * (SKIP_STEP_MS / 1000).toInt(),
                                nonce = (prev?.nonce ?: 0) + 1
                            )
                            ripples.add(TapRipple(System.nanoTime(), offset.x, offset.y))
                        },
                        onLongPressStart = {
                            if (holder.isPlaying) {
                                isFastForwarding = true
                                holder.overridePlaybackSpeed(FAST_FORWARD_SPEED)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        },
                        onLongPressEnd = {
                            if (isFastForwarding) {
                                isFastForwarding = false
                                holder.restorePlaybackSpeed()
                            }
                        }
                    )
                }
        )

        TapRipples(ripples)

        if (holder.isBuffering && !isFastForwarding) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        AnimatedVisibility(
            visible = isFastForwarding,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        ) {
            PlayerChip {
                Icon(
                    Icons.Default.FastForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(formatSpeed(FAST_FORWARD_SPEED), color = Color.White)
            }
        }

        skipFeedback?.let { fb ->
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = if (fb.isForward) Alignment.CenterEnd
                else Alignment.CenterStart
            ) {
                Box(Modifier.padding(horizontal = 48.dp)) {
                    PlayerChip {
                        Icon(
                            if (fb.isForward) Icons.Default.Forward10
                            else Icons.Default.Replay10,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.player_skip_seconds, fb.seconds),
                            color = Color.White
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            PlayerControls(
                isPlaying = holder.isPlaying,
                isBuffering = holder.isBuffering,
                hasEnded = holder.hasEnded,
                position = { holder.position },
                duration = holder.duration,
                scrubbing = scrubbing,
                scrubValue = scrubValue,
                playbackSpeed = holder.playbackSpeed,
                speedMenuOpen = speedMenuOpen,
                downloading = holder.downloading,
                onPlayPause = {
                    holder.togglePlayPause()
                    controlsVisible = true
                },
                onBack = { Navigator.back() },
                onDownload = {
                    holder.download()
                    controlsVisible = true
                },
                onSpeedMenuOpenChange = { open ->
                    speedMenuOpen = open
                    if (open) controlsVisible = true
                },
                onSpeedSelected = { speed ->
                    holder.selectPlaybackSpeed(speed)
                    speedMenuOpen = false
                },
                onScrub = { value ->
                    scrubbing = true
                    scrubValue = value
                    controlsVisible = true
                },
                onScrubFinished = {
                    val duration = holder.duration
                    if (duration > 0) holder.seekTo((scrubValue * duration).toLong())
                    scrubbing = false
                },
            )
        }
    }
}

@Composable
private fun PlayerChip(content: @Composable () -> Unit) {
    Surface(
        color = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

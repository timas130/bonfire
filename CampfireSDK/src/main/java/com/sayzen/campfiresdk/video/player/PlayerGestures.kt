package com.sayzen.campfiresdk.video.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal data class SkipFeedback(val isForward: Boolean, val seconds: Int, val nonce: Int)

internal data class TapRipple(
    val id: Long,
    val x: Float,
    val y: Float,
)

private const val SINGLE_TAP_RESOLVE_MS = 300L
private const val MULTI_TAP_WINDOW_MS = 1000L

/**
 * Media-player gestures: center/side single tap, YouTube-style chained multi-tap
 * seeking on either half, and long-press for temporary fast-forward.
 */
internal suspend fun PointerInputScope.detectMediaGestures(
    onSingleTap: (Offset) -> Unit,
    onMultiTap: (count: Int, isRight: Boolean, position: Offset) -> Unit,
    onLongPressStart: () -> Unit,
    onLongPressEnd: () -> Unit,
) = coroutineScope {
    var chainSide: Boolean? = null
    var chainCount = 0
    var resolveJob: Job? = null
    val longPressTimeout = viewConfiguration.longPressTimeoutMillis

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Main)
        val side = down.position.x > size.width / 2f
        val centerHalfBand = size.width * 0.15f
        val isCenter =
            kotlin.math.abs(down.position.x - size.width / 2f) < centerHalfBand

        val tap: PointerInputChange? = try {
            withTimeout(longPressTimeout) {
                waitForUpOrCancellation()
            }
        } catch (_: PointerEventTimeoutCancellationException) {
            resolveJob?.cancel()
            chainSide = null
            chainCount = 0
            onLongPressStart()
            try {
                waitForUpOrCancellation()
            } finally {
                onLongPressEnd()
            }
            return@awaitEachGesture
        }

        if (tap == null) return@awaitEachGesture // child claimed

        if (isCenter) {
            resolveJob?.cancel()
            chainCount = 0
            chainSide = null
            onSingleTap(down.position)
            return@awaitEachGesture
        }

        val continuesChain = chainCount > 0 && chainSide == side
        if (continuesChain) {
            chainCount += 1
            onMultiTap(chainCount, side, down.position)
        } else {
            chainCount = 1
            chainSide = side
        }
        resolveJob?.cancel()
        val countSnapshot = chainCount
        resolveJob = launch {
            val delay = if (countSnapshot == 1) SINGLE_TAP_RESOLVE_MS
            else MULTI_TAP_WINDOW_MS
            delay(delay.milliseconds)
            if (countSnapshot == 1) onSingleTap(down.position)
            chainCount = 0
            chainSide = null
        }
    }
}

@Composable
internal fun TapRipples(ripples: SnapshotStateList<TapRipple>) {
    ripples.forEach { ripple ->
        key(ripple.id) {
            val progress = remember { Animatable(0f) }
            LaunchedEffect(ripple.id) {
                try {
                    progress.animateTo(1f, animationSpec = tween(550))
                } finally {
                    ripples.remove(ripple)
                }
            }
            Canvas(Modifier.fillMaxSize()) {
                val p = progress.value
                val alpha = (1f - p).coerceIn(0f, 1f) * 0.28f
                val radius = 60.dp.toPx() + p * 220.dp.toPx()
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(ripple.x, ripple.y)
                )
            }
        }
    }
}

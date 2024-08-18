package sh.sit.bonfire.formatting.compose

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Shader
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.util.fastForEach
import androidx.core.graphics.ColorUtils
import com.posthog.PostHog
import com.sup.dev.android.tools.ToolsPerformance
import com.sup.dev.java.tools.ToolsMath
import kotlinx.coroutines.launch
import sh.sit.bonfire.formatting.R
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.properties.Delegates

@Composable
fun LinksClickableText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    background: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    maxLines: Int = Int.MAX_VALUE,
) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    var spoilerRevealPos by remember { mutableStateOf(Offset.Zero) }
    val spoilerRevealAnim = remember { Animatable(0f) }

    // click handler
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val onClick = createTextOnClick(
        blockText = text,
        onSpoilerReveal = { offset ->
            if (spoilerRevealAnim.targetValue == 1f) {
                return@createTextOnClick false
            }

            scope.launch {
                spoilerRevealPos = offset
                spoilerRevealAnim.animateTo(1f, tween(1000))
            }
            true
        },
        context = LocalContext.current
    )
    val clickModifier = Modifier.pointerInput(onClick) {
        awaitEachGesture {
            val down = awaitFirstDown()
            val up = withTimeout(viewConfiguration.longPressTimeoutMillis) {
                waitForUpOrCancellation()
            }

            if (up == null) {
                return@awaitEachGesture
            }

            layoutResult.value?.let {
                val consume = onClick(it.getOffsetForPosition(up.position), up.position)
                if (consume) {
                    down.consume()
                    up.consume()
                }
            }
        }
    }

    // compute ||spoiler|| animation clip mask
    val spoilerPath = remember(layoutResult.value) {
        val layout = layoutResult.value ?: return@remember null

        val spoilerSpans = text.getStringAnnotations(SpoilerSpanTag, 0, text.length)
        if (spoilerSpans.isEmpty()) {
            return@remember null
        }

        val path = Path()
        for (span in spoilerSpans) {
            path.addPath(layout.getPathForRange(span.start, span.end))
        }

        path
    }
    val spoilerPathBounds = remember(spoilerPath) {
        spoilerPath?.getBounds()
    }

    // get remote configuration for spoiler animation
    val spoilerFeatureData = remember(spoilerPath) {
        if (spoilerPath == null) return@remember null

        PostHog.getFeatureFlag("inline_spoilers")
        PostHog.getFeatureFlagPayload(
            key = "inline_spoilers",
            defaultValue = mapOf(
                "animation" to "per-device",
                "style" to "dots",
                "dots" to 2000
            )
        ) as? Map<*, *>
    }

//    val spoilerParticleSystem = remember(spoilerPath) {
//        if (spoilerPath == null) return@remember null
//
//        SpoilerParticleSystem(
//            canvasSize = spoilerPathBounds!!.size,
//            baseParticleColor = colors.tertiary,
//            dots = (spoilerFeatureData?.get("dots") as? Int?) ?: 2000
//        )
//    }

    var elapsedTime by remember { mutableStateOf(0L) }
    LaunchedEffect(spoilerPath) {
        // no animation on low-end devices
        when (spoilerFeatureData?.get("animation")) {
            "per-device" -> {
                if (ToolsPerformance.performanceClass == ToolsPerformance.PerformanceClass.Low) {
                    return@LaunchedEffect
                }
            }
            false, null -> return@LaunchedEffect
        }

        var frameTimes: MutableList<Long>? = ArrayList(100)

        while (spoilerRevealAnim.value != 1f) {
            withFrameMillis {
                if (frameTimes != null && elapsedTime != 0L) {
                    frameTimes!!.add(it - elapsedTime)

                    if (frameTimes!!.size >= 100) {
                        PostHog.capture(
                            "inline spoiler frame times",
                            properties = mapOf("frame times" to frameTimes!!)
                        )
                        frameTimes = null
                    }
                }

                elapsedTime = it
            }
        }
    }

    val paint = remember { NativePaint() }

    val spoilerModifier = Modifier.drawWithContent {
        drawContent()

        spoilerPath?.takeIf { spoilerRevealAnim.value != 1f }?.let { path ->
            SpoilerParticleSystem.init(
                colorScheme = colors,
                dots = (spoilerFeatureData?.get("dots") as? Int?) ?: 2000
            )

            val revealedPath = Path()
            revealedPath.addOval(Rect(spoilerRevealPos, spoilerPathBounds!!.diagonal * spoilerRevealAnim.value))
            revealedPath.op(path, revealedPath, PathOperation.Difference)

            clipPath(revealedPath) {
                if (spoilerFeatureData?.get("style") == "dots") {
                    drawRect(background)
                    val bitmap = SpoilerParticleSystem.drawCached(elapsedTime)
                    paint.shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)

                    drawIntoCanvas {
                        val canvas = it.nativeCanvas
                        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
                    }
                } else {
                    drawRect(colors.tertiary)
                }
            }
        }
    }

    Text(
        text = text,
        modifier = modifier then spoilerModifier then clickModifier,
        onTextLayout = { layoutResult.value = it },
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

private val Rect.diagonal: Float
    get() = sqrt(width.pow(2) + height.pow(2))

@OptIn(ExperimentalTextApi::class)
internal fun createTextOnClick(
    blockText: AnnotatedString,
    onSpoilerReveal: (offset: Offset) -> Boolean,
    context: Context,
) = fun(pos: Int, offset: Offset): Boolean {
    val urlSpan = blockText.getUrlAnnotations(pos, pos + 1).firstOrNull()
    val url = urlSpan?.item?.url
    if (url != null) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.link_open_fail),
                Toast.LENGTH_LONG
            ).show()
        }
        return true
    }

    val spoilerSpan = blockText.getStringAnnotations(SpoilerSpanTag, pos, pos + 1).firstOrNull()
    if (spoilerSpan != null) {
        return onSpoilerReveal(offset)
    }

    return false
}

class SpoilerParticleSystem(canvasSize: Size, baseParticleColor: Color, dots: Int = 2000) {
    private val paint = NativePaint()

    class Dot(canvasSize: Size, baseParticleColor: Int, private val sharedPaint: android.graphics.Paint) {
        private val size = ToolsMath.randomFloat(2f, 6f)

        private val ttl = ToolsMath.randomInt(2000, 10000)
        private val ttlOffset = ToolsMath.randomInt(0, ttl / 2)

        private val moveX = ToolsMath.randomFloat(-size * 4, size * 4)
        private val moveY = ToolsMath.randomFloat(-size * 4, size * 4)

        private val startX = ToolsMath.randomFloat(0f, canvasSize.width)
        private val startY = ToolsMath.randomFloat(0f, canvasSize.height)

        private val color = baseParticleColor
        private val baseAlpha = ToolsMath.randomFloat(0.4f, 0.85f)

        fun draw(canvas: NativeCanvas, elapsed: Long) {
            val progress = ((elapsed + ttlOffset) % ttl) / ttl.toFloat()

            val fadeAlpha = if (progress < 0.2) {
                progress / 0.2f
            } else if (progress > 0.8) {
                (1 - progress) / 0.2f
            } else {
                1f
            }

            val x = startX + moveX * progress
            val y = startY + moveY * progress

            val alpha = (baseAlpha * fadeAlpha * 255).toInt()
            sharedPaint.color = ColorUtils.setAlphaComponent(color, alpha)
            canvas.drawRect(
                x,
                y,
                x + size,
                y + size,
                sharedPaint,
            )
        }
    }

    private val baseParticleColorInt = baseParticleColor.toArgb()
    private var dots = List(dots) { Dot(canvasSize, baseParticleColorInt, paint) }

    fun draw(canvas: Canvas, elapsed: Long) {
        draw(canvas.nativeCanvas, elapsed)
    }
    fun draw(nativeCanvas: NativeCanvas, elapsed: Long) {
        dots.fastForEach { dot ->
            dot.draw(nativeCanvas, elapsed)
        }
    }

    companion object {
        private lateinit var colorScheme: ColorScheme
        private var dots by Delegates.notNull<Int>()
        private var initialized = false

        fun init(colorScheme: ColorScheme, dots: Int) {
            if (initialized) return

            this.colorScheme = colorScheme
            this.dots = dots
            this.initialized = true
        }

        private val bitmap: Bitmap by lazy {
            Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        }
        private val system: SpoilerParticleSystem by lazy {
            SpoilerParticleSystem(Size(300f, 300f), colorScheme.tertiary, dots)
        }

        private var currentElapsed = -1L

        fun drawCached(elapsed: Long): Bitmap {
            if (currentElapsed == elapsed) return bitmap

            bitmap.eraseColor(android.graphics.Color.TRANSPARENT)

            val canvas = NativeCanvas(bitmap)
            system.draw(canvas, elapsed)

            currentElapsed = elapsed

            return bitmap
        }
    }
}

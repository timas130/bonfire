package com.sayzen.campfiresdk.compose.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.node.Ref
import androidx.compose.ui.unit.LayoutDirection
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

fun Modifier.shimmerExt(visible: Boolean, shimmer: Shimmer? = null): Modifier {
    return composed {
        val lastSize = remember { Ref<Size>() }
        val lastLayoutDirection = remember { Ref<LayoutDirection>() }
        val lastOutline = remember { Ref<Outline>() }

        if (!visible) return@composed this

        val shape = MaterialTheme.shapes.small
        val color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
        then(
            Modifier
                .shimmer(shimmer ?: rememberShimmer(ShimmerBounds.View))
                .drawWithContent {
                    val outline = lastOutline.value.takeIf {
                        size == lastSize.value && layoutDirection == lastLayoutDirection.value
                    } ?: shape.createOutline(size, layoutDirection, this)

                    drawOutline(
                        outline = outline,
                        color = color,
                    )
                }
        )
    }
}

@Composable
fun BoxScope.RemoteImageShimmer(shimmer: Shimmer? = null) {
    Box(Modifier
        .matchParentSize()
        .shimmerExt(true, shimmer))
}

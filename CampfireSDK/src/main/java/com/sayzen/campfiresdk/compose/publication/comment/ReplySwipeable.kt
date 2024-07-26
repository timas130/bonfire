package com.sayzen.campfiresdk.compose.publication.comment

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.zIndex
import com.sayzen.campfiresdk.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ReplySwipeable(
    onReply: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dragWidth = 48.dp

    val density = LocalDensity.current
    val anchors = with(density) {
        DraggableAnchors {
            (-1) at -dragWidth.toPx()
            0 at 0f
            1 at dragWidth.toPx()
        }
    }
    val anchoredDragState = remember {
        AnchoredDraggableState(
            initialValue = 0,
            anchors = anchors,
            positionalThreshold = { totalDistance: Float -> totalDistance * 0.75f },
            velocityThreshold = { with(density) { dragWidth.toPx() } },
            snapAnimationSpec = spring(),
            decayAnimationSpec = exponentialDecay(),
        )
    }

    LaunchedEffect(anchoredDragState.settledValue) {
        if (anchoredDragState.settledValue == 0) return@LaunchedEffect

        onReply()

        anchoredDragState.animateTo(0)
    }

    Box(
        modifier
            .anchoredDraggable(
                state = anchoredDragState,
                orientation = Orientation.Horizontal,
                enabled = enabled,
            )
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .matchParentSize()
                .height(IntrinsicSize.Min)
                .zIndex(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            repeat(2) {
                Box(
                    Modifier
                        .width(dragWidth)
                        .fillMaxHeight()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.reply_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Box(
            Modifier
                .zIndex(2f)
                .offset {
                    IntOffset(
                        x = anchoredDragState
                            .requireOffset()
                            .fastRoundToInt(), y = 0
                    )
                }
        ) {
            content()
        }
    }
}

package com.sayzen.campfiresdk.compose.publication.comment

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.PublicationComment
import com.sayzen.campfiresdk.compose.ComposeCard
import kotlinx.coroutines.flow.MutableStateFlow

class ComposeCommentCard(
    private val initialComment: PublicationComment,
    var showFandom: Boolean = false,
    private val onRemoved: () -> Unit,
    private val scrollToComment: (PublicationComment) -> Unit = DefaultScrollToComment,
    private var maxLines: Int = Int.MAX_VALUE,
    private val allowSwipeReply: Boolean = false,
    private val allowEditing: Boolean = false,
    private val withPadding: Boolean = false,
    private val onCreated: (PublicationComment) -> Unit = {},
) : ComposeCard() {
    var maxTextSize: Int
        get() = maxLines * 100
        set(value) {
            maxLines = value / 100
        }

    private var flashState = MutableStateFlow(false)

    fun flash() {
        flashState.value = true
    }

    @Composable
    override fun Content() {
        val isFlashing by flashState.collectAsState()
        val flashValue by animateFloatAsState(
            if (isFlashing) 0.4f else 0f,
            animationSpec = tween(600),
            label = "CommentFlash"
        )

        Box(Modifier.drawWithContent {
            drawContent()
            drawRect(Color.White.copy(alpha = flashValue))
            if (flashValue == 0.4f && flashState.value) {
                flashState.value = false
            }
        }) {
            Comment(
                initialComment = initialComment,
                showFandom = showFandom,
                onRemoved = onRemoved,
                scrollToComment = scrollToComment,
                maxLines = maxLines,
                allowSwipeReply = allowSwipeReply,
                allowEditing = allowEditing,
                onCreated = onCreated,
                modifier = if (withPadding) {
                    Modifier.padding(vertical = 8.dp)
                } else {
                    Modifier
                }
            )
        }
    }

    @Composable
    override fun getBackground(): Color = Color.Transparent
}

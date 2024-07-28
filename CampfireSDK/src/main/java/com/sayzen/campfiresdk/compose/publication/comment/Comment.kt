package com.sayzen.campfiresdk.compose.publication.comment

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PublicationComment
import com.sayzen.campfiresdk.compose.publication.PublicationReactions
import com.sayzen.campfiresdk.compose.util.Avatar
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerComment
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.splashs.SplashComment

class CommentModel(comment: PublicationComment, onRemoved: State<() -> Unit>) : ViewModel() {
    val dataSource = CommentDataSource(comment, onRemoved)

    override fun onCleared() {
        super.onCleared()
        dataSource.destroy()
    }
}

internal val DefaultScrollToComment: (comment: PublicationComment) -> Unit = { comment ->
    ControllerPublications.toPublication(
        publicationType = comment.parentPublicationType,
        publicationId = comment.parentPublicationId,
        commentId = comment.id
    )
}

internal fun PublicationComment.makeQuoteText(): String = buildString {
    append(creator.name)
    append(": ")
    if (text.isNotEmpty()) {
        append(text)
    } else if (image.isNotEmpty() || images.isNotEmpty()) {
        append(t(API_TRANSLATE.app_image))
    } else if (stickerId != 0L) {
        append(t(API_TRANSLATE.app_sticker))
    }
}

@Composable
fun Comment(
    initialComment: PublicationComment,
    modifier: Modifier = Modifier,
    showFandom: Boolean = false,
    onRemoved: () -> Unit,
    scrollToComment: (PublicationComment) -> Unit = DefaultScrollToComment,
    maxLines: Int = Int.MAX_VALUE,
    allowSwipeReply: Boolean = false,
    allowEditing: Boolean = false,
    onCreated: (PublicationComment) -> Unit = {},
) {
    val view = LocalView.current

    val onRemovedRef = rememberUpdatedState(onRemoved)
    val model = viewModel(key = "Comment:${initialComment.id}") {
        CommentModel(initialComment, onRemovedRef)
    }

    val comment by model.dataSource.flow.collectAsState()

    val onReply: (showToast: Boolean) -> Unit = { showToast ->
        SplashComment(
            publicationId = comment.parentPublicationId,
            answer = comment,
            changeComment = null,
            quoteId = comment.id,
            quoteText = comment.makeQuoteText(),
            showToast = showToast,
            onCreated = onCreated,
        ).asSheetShow()
    }

    val globalCardPosition = remember { mutableStateOf<LayoutCoordinates?>(null) }

    ReplySwipeable(
        onReply = { onReply(false) },
        enabled = allowSwipeReply,
        modifier = modifier,
    ) {
        Card(
            shape = RectangleShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    globalCardPosition.value = it
                }
                .veryComplicatedClickable(
                    onClick = {
                        if (allowEditing && ControllerApi.isCurrentAccount(comment.creator.id)) {
                            SplashComment(
                                changeComment = comment,
                                showToast = true
                            ).asSheetShow()
                        } else if (!allowEditing && comment.status == API.STATUS_PUBLIC) {
                            scrollToComment(comment)
                        }
                    },
                    onLongClick = { offset ->
                        val viewOffset = IntArray(2)
                        view.getLocationInWindow(viewOffset)
                        val windowClickOffset = globalCardPosition.value!!.localToWindow(offset)

                        ControllerComment.showMenu(
                            view,
                            windowClickOffset.x - viewOffset[0],
                            windowClickOffset.y - viewOffset[1],
                            comment
                        )
                    }
                )
        ) {
            CommentContent(
                comment = comment,
                onReply = onReply,
                showFandom = showFandom,
                scrollToComment = scrollToComment,
                maxLines = maxLines
            )
        }
    }
}

@Composable
private fun CommentContent(
    comment: PublicationComment,
    onReply: (showToast: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showFandom: Boolean = false,
    scrollToComment: (id: PublicationComment) -> Unit = DefaultScrollToComment,
    maxLines: Int = Int.MAX_VALUE,
) {
    Row(
        modifier = modifier.padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showFandom) {
            Avatar(
                fandom = comment.fandom,
                modifier = Modifier.size(32.dp),
            )
        } else {
            Avatar(
                account = comment.creator,
                modifier = Modifier.size(32.dp),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CommentHeader(comment, showFandom = showFandom)

            CommentQuote(comment, scrollToComment = scrollToComment)

            CommentText(comment, maxLines = maxLines)

            CommentImage(comment)

            PublicationReactions(comment)

            if (comment.status == API.STATUS_PUBLIC) {
                CommentFooter(comment, onReply)
            }
        }
    }
}

private fun Modifier.veryComplicatedClickable(
    onClick: (Offset) -> Unit,
    onLongClick: (Offset) -> Unit,
) = composed {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    Modifier
        .indication(interactionSource, LocalIndication.current)
        .pointerInput(onClick, onLongClick) {
            awaitEachGesture {
                // when CommentQuote is pressed, it consumes the down event.
                // we don't care though because root Comment needs to handle long clicks.
                val down = awaitFirstDown(requireUnconsumed = false)
                val press = PressInteraction.Press(down.position)

                // we can show the indication if it has not been shown in CommentQuote
                val emitInteractions = !down.isConsumed
                if (emitInteractions) {
                    interactionSource.tryEmit(press)
                    down.consume()
                }

                try {
                    val up = withTimeout(viewConfiguration.longPressTimeoutMillis) {
                        waitForUpOrCancellation()
                    }
                    if (up != null) {
                        // on click
                        up.consume()
                        if (emitInteractions) {
                            interactionSource.tryEmit(PressInteraction.Release(press))
                        }
                        onClick(down.position)
                    } else {
                        // canceled (or passed to CommentQuote)
                        if (emitInteractions) {
                            interactionSource.tryEmit(PressInteraction.Cancel(press))
                        }
                    }
                } catch (e: PointerEventTimeoutCancellationException) {
                    // on long click

                    if (!emitInteractions) {
                        interactionSource.tryEmit(press)
                    }

                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick(down.position)

                    if (waitForUpOrCancellation() != null) {
                        interactionSource.tryEmit(PressInteraction.Release(press))
                    } else {
                        interactionSource.tryEmit(PressInteraction.Cancel(press))
                    }
                }
            }
        }
}

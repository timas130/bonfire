package com.sayzen.campfiresdk.compose.publication.comment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PublicationComment
import com.sayzen.campfiresdk.compose.publication.PublicationReactions
import com.sayzen.campfiresdk.compose.util.Avatar
import com.sayzen.campfiresdk.compose.util.IconButtonWithOffset
import com.sayzen.campfiresdk.compose.util.relativeToView
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
    val longClickEnabledFlag = remember { mutableStateOf(true) }

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
                .nestedClickableRoot(
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
                        val windowClickOffset = globalCardPosition.value!!.localToWindow(offset)
                        val relativeOffset = windowClickOffset.relativeToView(view)

                        ControllerComment.showMenu(
                            view,
                            relativeOffset.x,
                            relativeOffset.y,
                            comment
                        )
                    },
                    longClickEnabledFlag = longClickEnabledFlag,
                )
        ) {
            CommentContent(
                comment = comment,
                onReply = onReply,
                showFandom = showFandom,
                scrollToComment = scrollToComment,
                maxLines = maxLines,
                longClickEnabledFlag = longClickEnabledFlag,
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
    longClickEnabledFlag: MutableState<Boolean> = remember { mutableStateOf(true) },
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
                onLongClick = {
                    longClickEnabledFlag.value = false
                },
                modifier = Modifier.size(32.dp),
            )
        }

        Box {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CommentHeader(comment, showFandom = showFandom)

                CommentQuote(comment, scrollToComment = scrollToComment)

                CommentText(comment, maxLines = maxLines)

                CommentImage(comment)

                PublicationReactions(comment, onLongClick = { longClickEnabledFlag.value = false })

                if (comment.status == API.STATUS_PUBLIC) {
                    CommentFooter(comment, onReply, longClickEnabledFlag)
                }
            }

            val view = LocalView.current
            IconButtonWithOffset(
                onClick = { offset ->
                    ControllerComment.showMenu(view, offset.x, offset.y, comment)
                },
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}

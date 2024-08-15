package com.sayzen.campfiresdk.compose.publication.comment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.PublicationComment
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.publication.KarmaCounter
import com.sayzen.campfiresdk.compose.publication.ReportsButton

@Composable
internal fun CommentFooter(
    comment: PublicationComment,
    onReply: (showToast: Boolean) -> Unit,
    longClickEnabledFlag: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    ) {
        ReportsButton(comment, Modifier.height(32.dp))

        ReplyButton(onClick = { onReply(true) })

        KarmaCounter(
            publication = comment,
            mini = true,
            onLongClick = {
                longClickEnabledFlag.value = false
            }
        )
    }
}

@Composable
private fun ReplyButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val theme = ButtonDefaults.filledTonalButtonColors()

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Surface(
            shape = ButtonDefaults.filledTonalShape,
            color = theme.containerColor,
            contentColor = theme.contentColor,
            modifier = modifier
                .clip(ButtonDefaults.filledTonalShape)
                .nestedClickable {
                    onClick()
                },
        ) {
            Row(
                modifier = Modifier
                    .height(32.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.reply_24px),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))

                Text(
                    text = stringResource(R.string.comment_reply),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

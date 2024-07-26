package com.sayzen.campfiresdk.compose.publication.comment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.PublicationComment
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.views.SplashAccountInfo
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.java.tools.ToolsDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CommentHeader(
    comment: PublicationComment,
    modifier: Modifier = Modifier,
    showFandom: Boolean = false,
) {
    val hapticFeedback = LocalHapticFeedback.current

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        Text(
            text = if (showFandom) {
                comment.fandom.name
            } else {
                comment.creator.name
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = if (showFandom) {
                Modifier.clickable {
                    SFandom.instance(comment.fandom, Navigator.TO)
                }
            } else {
                Modifier.combinedClickable(
                    onClick = {
                        SProfile.instance(comment.creator, Navigator.TO)
                    },
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        SplashAccountInfo(comment.creator).asSheetShow()
                    }
                )
            }
        )

        Text(
            text = buildString {
                append(ToolsDate.dateToString(comment.dateCreate))
                if (comment.changed) {
                    append(' ')
                    append(stringResource(R.string.comment_edited))
                }
            },
            style = MaterialTheme.typography.bodyLarge,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            modifier = Modifier.alpha(0.6f)
        )
    }
}

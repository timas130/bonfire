package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.post.PageVideo
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsToast
import sh.sit.bonfire.images.RemoteImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PageVideoRenderer(page: PageVideo) {
    val hapticFeedback = LocalHapticFeedback.current

    Box {
        RemoteImage(
            link = page.image,
            contentDescription = stringResource(R.string.video_alt),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        ToolsAndroid.setToClipboard("https://youtu.be/${page.videoId}")
                        ToolsToast.show(R.string.link_copied)
                    },
                    onClick = {
                        ToolsIntent.openLink("https://youtu.be/${page.videoId}")
                    }
                )
        )

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp)
                .size(48.dp)
        )
    }
}

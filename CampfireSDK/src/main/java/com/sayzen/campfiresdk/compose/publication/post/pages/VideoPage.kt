package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    RemoteImage(
        link = page.image,
        contentDescription = stringResource(R.string.video_alt),
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .aspectRatio(16 / 9f)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onLongClick = {
                    ToolsAndroid.setToClipboard("https://youtu.be/${page.videoId}")
                    ToolsToast.show(R.string.link_copied)
                },
                onClick = {
                    ToolsIntent.openLink("https://youtu.be/${page.videoId}")
                }
            )
    )
}

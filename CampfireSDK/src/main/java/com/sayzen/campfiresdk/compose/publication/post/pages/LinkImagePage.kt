package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.models.publications.post.PageLinkImage
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsToast
import sh.sit.bonfire.images.RemoteImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PageLinkImageRenderer(page: PageLinkImage) {
    Box(Modifier.padding(horizontal = 12.dp)) {
        RemoteImage(
            link = page.image,
            contentDescription = stringResource(R.string.page_link_image),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 1f)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onLongClick = {
                        ToolsAndroid.setToClipboard(page.link)
                        ToolsToast.show(R.string.link_copied)
                    },
                    onClick = {
                        ToolsIntent.openLink(page.link)
                    }
                )
        )

        Icon(
            painter = painterResource(R.drawable.ic_insert_link_white_24dp),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
                .padding(8.dp)
        )
    }
}

@Preview
@Composable
internal fun PageLinkImagePreview() {
    Surface {
        PageLinkImageRenderer(page = PageLinkImage().apply {
            image = ImageRef()
            link = "https://google.com"
        })
    }
}

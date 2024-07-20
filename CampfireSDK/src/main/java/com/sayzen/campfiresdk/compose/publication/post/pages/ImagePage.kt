package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.post.PageImage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SImageView
import sh.sit.bonfire.images.RemoteImage
import sh.sit.bonfire.images.RemoteImageLoader

@Composable
internal fun PageImageRenderer(page: PageImage) {
    Box(Modifier.fillMaxWidth()) {
        RemoteImage(
            link = page.image,
            gifLink = page.gif,
            loader = { RemoteImageLoader() },
            contentDescription = stringResource(R.string.page_image),
            matchHeightConstraintsFirst = true,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .heightIn(max = 700.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    val link = ImageLoader.load(page.gif.takeIf { it.isNotEmpty() } ?: page.image)
                    Navigator.to(SImageView(link))
                }
        )
    }
}

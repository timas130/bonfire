package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.post.PageImages
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SImageView
import sh.sit.bonfire.images.RemoteImage
import sh.sit.bonfire.images.RemoteImageLoader

@Composable
internal fun PageImagesRenderer(page: PageImages) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .heightIn(max = 180.dp)
    ) {
        itemsIndexed(page.imagesMini) { index, ref ->
            RemoteImage(
                link = ref,
                loader = { RemoteImageLoader() },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val links = page.images.map { ImageLoader.load(it) }.toTypedArray()
                        Navigator.to(SImageView(index, links))
                    }
            )
        }
    }
}

package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.models.publications.post.PageImages
import com.sayzen.campfiresdk.compose.util.RemoteImageShimmer
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SImageView
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import sh.sit.bonfire.images.RemoteImage

private val imagesColumnCounts = mapOf(
    1 to listOf(1),
    2 to listOf(2),
    3 to listOf(3),
    4 to listOf(2, 2),
    5 to listOf(2, 3),
    6 to listOf(4, 2),
    7 to listOf(4, 3),
    8 to listOf(2, 3, 3),
    9 to listOf(2, 3, 4),
    10 to listOf(3, 4, 3)
)

@Composable
internal fun PageImagesRenderer(page: PageImages) {
    if (page.mode == PageImages.MODE_GRID) {
        FixedGridPageImagesVariant(
            images = page.images,
            imagesMini = page.imagesMini,
            modifier = Modifier
                .padding(horizontal = 12.dp)
        )
    } else {
        ScrollablePageImagesVariant(page)
    }
}

@Composable
fun FixedGridPageImagesVariant(
    images: Array<ImageRef>,
    modifier: Modifier = Modifier,
    imagesMini: Array<ImageRef> = images,
) {
    val columnCounts = imagesColumnCounts[imagesMini.size]
    if (columnCounts == null) {
        Text(
            "[render error: too many/little images (${imagesMini.size})]",
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        return
    }

    val spacing = 2.dp

    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
    ) {
        val iter = imagesMini.iterator().withIndex()

        for (count in columnCounts) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                repeat(count) {
                    val imageMini = iter.next()

                    RemoteImage(
                        link = imageMini.value,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        loader = { RemoteImageShimmer(shimmer) },
                        forceAspectRatio = false,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val links = images
                                    .map { ImageLoader.load(it) }
                                    .toTypedArray()
                                Navigator.to(SImageView(imageMini.index, links))
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrollablePageImagesVariant(page: PageImages) {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .heightIn(max = 180.dp)
    ) {
        itemsIndexed(page.imagesMini) { index, ref ->
            RemoteImage(
                link = ref,
                loader = { RemoteImageShimmer(shimmer) },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val links = page.images
                            .map { ImageLoader.load(it) }
                            .toTypedArray()
                        Navigator.to(SImageView(index, links))
                    }
            )
        }
    }
}

package com.sayzen.campfiresdk.compose.attach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.sayzen.campfiresdk.compose.util.RemoteImageShimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer

@Composable
internal fun GalleryTab(model: AttachFlyoutModel) {
    val shimmer = rememberShimmer(ShimmerBounds.Window)
    val listState = rememberLazyGridState()

    val totalGalleryImages by model.totalGalleryImages.collectAsState()
    val galleryFilter by model.galleryFilter.collectAsState()

    LaunchedEffect(galleryFilter) {
        listState.requestScrollToItem(0)
    }

    LazyVerticalGrid(
        state = listState,
        columns = GridCells.Adaptive(130.dp),
        contentPadding = PaddingValues(start = 4.dp, end = 4.dp, bottom = (48 + 8).dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(totalGalleryImages, key = { model.getGalleryImage(it)?.id ?: -it }) { index ->
            val imageFile = model.getGalleryImage(index)?.file ?: return@items

            SubcomposeAsyncImage(
                model = imageFile,
                contentDescription = "",
                modifier = Modifier.aspectRatio(1f),
                loading = {
                    RemoteImageShimmer(shimmer)
                },
                contentScale = ContentScale.Crop,
            )
        }
    }
}

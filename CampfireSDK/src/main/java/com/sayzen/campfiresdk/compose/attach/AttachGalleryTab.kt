package com.sayzen.campfiresdk.compose.attach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.util.ErrorCard
import com.sayzen.campfiresdk.compose.util.RemoteImageShimmer
import com.sup.dev.android.tools.ToolsPermission
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer

@Composable
internal fun GalleryTab(model: AttachFlyoutModel) {
    val shimmer = rememberShimmer(ShimmerBounds.Window)
    val listState = rememberLazyGridState()

    val totalGalleryImages by model.totalGalleryImages.collectAsState()
    val galleryFilter by model.galleryFilter.collectAsState()
    val galleryPermissionGranted by model.galleryPermissionGranted.collectAsState()

    LaunchedEffect(galleryFilter) {
        listState.requestScrollToItem(0)
    }

    val pagerScrollEnabled by derivedStateOf {
        listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 100
    }
    LaunchedEffect(pagerScrollEnabled) {
        model.pagerScrollAllowed.value = pagerScrollEnabled
    }

    LazyVerticalGrid(
        state = listState,
        columns = GridCells.Adaptive(130.dp),
        contentPadding = PaddingValues(start = 4.dp, end = 4.dp, bottom = (48 + 8).dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (!galleryPermissionGranted) {
            item(key = "permission") {
                Column(
                    modifier = Modifier.animateItem(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ErrorCard(stringResource(R.string.attach_gallery_no_permission))

                    TextButton(onClick = { ToolsPermission.navigateToSettings() }) {
                        Text(stringResource(R.string.attach_gallery_permission_settings))
                    }
                }
            }
        }

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

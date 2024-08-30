package com.sayzen.campfiresdk.compose.attach

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.publication.post.counterTransitionSpec
import com.sayzen.campfiresdk.compose.util.ErrorCard
import com.sayzen.campfiresdk.compose.util.RemoteImageShimmer
import com.sup.dev.android.tools.ToolsPermission
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun GalleryTab(
    model: AttachFlyoutModel,
    sharedTransitionScope: SharedTransitionScope,
) {
    val shimmer = rememberShimmer(ShimmerBounds.Window)
    val listState = rememberLazyGridState()

    val totalGalleryImages by model.totalGalleryImages.collectAsState()
    val galleryFilter by model.galleryFilter.collectAsState()
    val galleryPermissionGranted by model.galleryPermissionGranted.collectAsState()

    LaunchedEffect(listState) {
        snapshotFlow { galleryFilter }
            .distinctUntilChanged()
            .collect {
                listState.requestScrollToItem(0)
            }
    }
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 100
        }.collect {
            model.pagerScrollAllowed.value = it
        }
    }

    LazyVerticalGrid(
        state = listState,
        columns = GridCells.Adaptive(130.dp),
        contentPadding = PaddingValues(start = 4.dp, end = 4.dp, bottom = (48 + 8).dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (!galleryPermissionGranted) {
            item(key = "permission", span = { GridItemSpan(3) }) {
                Column(
                    modifier = Modifier.animateItem(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ErrorCard(stringResource(R.string.attach_gallery_no_permission))

                    TextButton(onClick = { ToolsPermission.navigateToSettings() }) {
                        Text(stringResource(R.string.attach_gallery_permission_settings))
                    }
                }
            }
        }

        items(totalGalleryImages, key = { model.getGalleryImage(it)?.id ?: -it }) { index ->
            GalleryItem(
                index = index,
                model = model,
                shimmer = shimmer,
                sharedTransitionScope = sharedTransitionScope,
                modifier = Modifier.animateItem()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun GalleryItem(
    index: Int,
    model: AttachFlyoutModel,
    shimmer: Shimmer,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier
) {
    // force recompose when filter changes
    model.galleryFilter.collectAsState().value

    val galleryImage = model.getGalleryImage(index) ?: return

    val hapticFeedback = LocalHapticFeedback.current

    val selectedImageIdx by model.selectedImageIndex(galleryImage.id).collectAsState()
    val openedImage by model.openedImage.collectAsState()

    val thisImageOpened by derivedStateOf {
        openedImage?.id == galleryImage.id
    }

    Box(modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))) {
        val imageScale by animateFloatAsState(
            targetValue = if (selectedImageIdx != -1) 0.8f else 1f,
            label = "GalleryItemImageScale"
        )

        with(sharedTransitionScope) {
            SubcomposeAsyncImage(
                model = galleryImage.file,
                contentDescription = "",
                contentScale = ContentScale.Crop,
                loading = {
                    RemoteImageShimmer(shimmer)
                },
                modifier = Modifier
                    .aspectRatio(1f)
                    .combinedClickable(
                        onClick = {
                            model.openImage(galleryImage)
                        },
                        onLongClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            model.setImageSelected(galleryImage, true)
                        }
                    )
                    .sharedElementWithCallerManagedVisibility(
                        sharedContentState = rememberSharedContentState("gallery:${galleryImage.id}"),
                        visible = !thisImageOpened,
                    )
                    .graphicsLayer {
                        scaleX = imageScale
                        scaleY = imageScale
                    }
            )
        }

        val checkboxProgress by animateFloatAsState(
            targetValue = if (selectedImageIdx != -1) 0f else 1f,
            label = "GalleryItemCheckbox"
        )
        val path1 = remember { Path() }
        val path2 = remember { Path() }

        val checkboxColor = MaterialTheme.colorScheme.tertiary

        Box(
            Modifier
                .align(Alignment.TopEnd)
                .clickable(interactionSource = null, indication = null) {
                    model.setImageSelected(galleryImage, selectedImageIdx == -1)
                }
                .padding(6.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                .drawWithContent {
                    path1.reset()
                    path2.reset()
                    path1.addOval(size.toRect())
                    drawPath(path1, checkboxColor.copy(alpha = 0.2f))

                    path2.addOval(Rect(center, size.width / 2 * checkboxProgress))
                    path1.op(path1, path2, PathOperation.Difference)

                    drawPath(path1, checkboxColor)

                    drawContent()
                }
                .size(24.dp)
        ) {
            AnimatedContent(
                targetState = selectedImageIdx.toLong(),
                transitionSpec = counterTransitionSpec,
                label = "GalleryImageIndex",
                modifier = Modifier.align(Alignment.Center)
            ) { idx ->
                if (idx != -1L) {
                    Text(
                        text = (idx + 1).toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }
    }
}

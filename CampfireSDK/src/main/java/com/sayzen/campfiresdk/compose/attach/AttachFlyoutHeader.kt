package com.sayzen.campfiresdk.compose.attach

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.fastRoundToInt
import coil.compose.SubcomposeAsyncImage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.util.RemoteImageShimmer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AttachFlyoutHeader(model: AttachFlyoutModel, sheetState: SheetState, onDismissRequest: () -> Unit) {
    Layout(
        contents = listOf(
            {
                Surface(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                ) {
                    Box(Modifier.size(width = 32.dp, height = 4.dp))
                }
            },
            {
                AttachFlyoutTopAppBar(
                    model = model,
                    onDismissRequest = onDismissRequest,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = -sheetState
                                    .requireOffset()
                                    .fastRoundToInt()
                            )
                        }
                )
            }
        ),
        measurePolicy = { measurables, constraints ->
            val dragHandle = measurables[0].single().measure(constraints)
            val appBar = measurables[1].single().measure(constraints)

            val offset = try {
                sheetState.requireOffset()
            } catch (e: Exception) {
                0f
            }

            val heightDiff = (appBar.height - dragHandle.height).toFloat()
            val revealProgress = 1f - (offset / appBar.height).fastCoerceAtMost(1f)
            val height = dragHandle.height + heightDiff * revealProgress

            layout(constraints.maxWidth, height.fastRoundToInt()) {
                dragHandle.place(
                    x = constraints.maxWidth / 2 - dragHandle.width / 2,
                    y = 0
                )

                appBar.place(IntOffset.Zero)
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AttachFlyoutTopAppBar(
    model: AttachFlyoutModel,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTab by model.activeTab.collectAsState()

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onDismissRequest) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.attach_modal_close))
            }
        },
        title = {
            AnimatedContent(activeTab, label = "TabHeader", modifier = Modifier.fillMaxWidth()) { tab ->
                when (tab) {
                    AttachFlyoutModel.Tab.Gallery -> {
                        GalleryFilter(model)
                    }
                    AttachFlyoutModel.Tab.Gif -> {
                        Text(stringResource(R.string.attach_tab_gif))
                    }
                    AttachFlyoutModel.Tab.Stickers -> {
                        Text(stringResource(R.string.attach_tab_stickers))
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GalleryFilter(model: AttachFlyoutModel) {
    val galleryFilter by model.galleryFilter.collectAsState()
    val galleryAlbums by model.galleryAlbums.collectAsState()
    var albumDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(albumDropdownExpanded) {
        if (albumDropdownExpanded) model.loadGalleryAlbums()
    }

    ExposedDropdownMenuBox(
        expanded = albumDropdownExpanded,
        onExpandedChange = { albumDropdownExpanded = it }
    ) {
        Surface(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            color = Color.Transparent,
            contentColor = LocalContentColor.current
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AnimatedContent(galleryFilter?.name ?: stringResource(R.string.attach_album_all), label = "AlbumName") {
                    Text(
                        text = it,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                val rotation by animateFloatAsState(if (albumDropdownExpanded) 180f else 0f, label = "DropdownArrow")
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = rotation
                    }
                )
            }
        }

        ExposedDropdownMenu(
            expanded = albumDropdownExpanded,
            onDismissRequest = { albumDropdownExpanded = false },
            matchTextFieldWidth = false
        ) {
            galleryAlbums?.forEach { albumLike ->
                val album = albumLike as? AttachFlyoutModel.Album

                DropdownMenuItem(
                    leadingIcon = {
                        SubcomposeAsyncImage(
                            model = album?.preview?.file,
                            contentDescription = null,
                            loading = { RemoteImageShimmer() },
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(24.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    },
                    text = {
                        Text(
                            text = album?.name ?: stringResource(R.string.attach_album_all),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = when (albumLike) {
                                is AttachFlyoutModel.Album -> albumLike.elements
                                is AttachFlyoutModel.AllAlbums -> albumLike.elements
                            }.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.alpha(0.6f)
                        )
                    },
                    onClick = {
                        model.setGalleryFilter(albumLike)
                        albumDropdownExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

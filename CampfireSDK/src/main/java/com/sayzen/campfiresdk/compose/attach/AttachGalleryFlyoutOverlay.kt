package com.sayzen.campfiresdk.compose.attach

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.util.AnimatedNullableVisibility
import com.smarttoolfactory.cropper.ImageCropper
import com.smarttoolfactory.cropper.model.OutlineType
import com.smarttoolfactory.cropper.model.RectCropShape
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.smarttoolfactory.cropper.settings.CropOutlineProperty

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun SharedTransitionScope.AttachFlyoutOverlay(model: AttachFlyoutModel) {
    val openedImageNullable by model.openedImage.collectAsState()

    AnimatedNullableVisibility(openedImageNullable, Modifier.fillMaxSize()) { openedImage ->
        val density = LocalDensity.current
        val draggableState = remember(density) {
            AnchoredDraggableState(
                initialValue = 0,
                anchors = DraggableAnchors {
                    (-1) at with(density) { -640000.dp.toPx() }
                    0 at 0f
                    (1) at with(density) { 640000.dp.toPx() }
                },
                positionalThreshold = { totalDistance: Float -> totalDistance / 10000f },
                velocityThreshold = { with(density) { 64.dp.toPx() } },
                animationSpec = SpringSpec(stiffness = 0f),
                confirmValueChange = {
                    if (it != 0) {
                        model.closeImage()
                    }
                    true
                }
            )
        }

        val croppingImage by model.croppingImage.collectAsState()

        Box(
            Modifier
                .semantics { dialog() }
                .background(MaterialTheme.colorScheme.scrim)
                .fillMaxSize()
                .pointerInput(Unit) {}
                .anchoredDraggable(
                    state = draggableState,
                    enabled = croppingImage == null,
                    orientation = Orientation.Vertical
                )
        ) {
            val paddingRatio by animateFloatAsState(if (croppingImage != null) 0.1f else 0f, label = "CropOverlayAnimation")

            if (paddingRatio != 0.1f) {
                AsyncImage(
                    model = openedImage.file,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .layout { measurable, constraints ->
                            val widthOffset = (constraints.maxWidth * paddingRatio * 2).fastRoundToInt()
                            val heightOffset = (constraints.maxHeight * paddingRatio * 2).fastRoundToInt()

                            val placeable = measurable.measure(
                                constraints.copy(
                                    maxWidth = constraints.maxWidth - widthOffset,
                                    maxHeight = constraints.maxHeight - heightOffset
                                )
                            )

                            layout(placeable.width, placeable.height) {
                                placeable.place(widthOffset / 2, heightOffset / 2)
                            }
                        }
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .aspectRatio(
                            ratio = openedImage.width / openedImage.height.toFloat(),
                            matchHeightConstraintsFirst = true
                        )
                        .zIndex(-10f)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = draggableState
                                    .requireOffset()
                                    .fastRoundToInt()
                            )
                        }
                        .sharedElementWithCallerManagedVisibility(
                            sharedContentState = rememberSharedContentState("gallery:${openedImage.id}"),
                            visible = openedImageNullable?.id == openedImage.id
                        )
                )
            } else {
                CropOverlay(model)
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(0.3f))
                    .padding(16.dp)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                    )
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
            ) {
                IconButton(onClick = { model.startImageCrop() }) {
                    Icon(
                        painter = painterResource(R.drawable.crop_24px),
                        contentDescription = stringResource(R.string.attach_gallery_crop)
                    )
                }
            }

            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { model.closeImage() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                title = {
                    Text(stringResource(R.string.attach_gallery_photo))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .align(Alignment.TopStart)
            )
        }
    }
}

@Composable
private fun CropOverlay(model: AttachFlyoutModel) {
    val croppingImage by model.croppingImage.collectAsState()

    if (croppingImage != null) {
        ImageCropper(
            imageBitmap = croppingImage!!,
            contentDescription = "",
            cropProperties = CropDefaults.properties(
                handleSize = with(LocalDensity.current) { 24.dp.toPx() },
                overlayRatio = 1f,
                cropOutlineProperty = CropOutlineProperty(
                    outlineType = OutlineType.Rect,
                    cropOutline = RectCropShape(1, "Rect")
                ),
            ),
            onCropSuccess = {},
            onCropStart = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

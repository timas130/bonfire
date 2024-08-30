package com.sayzen.campfiresdk.compose.attach

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import coil.compose.AsyncImage
import com.mr0xf00.easycrop.ui.CropperPreview
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.util.AnimatedNullableVisibility

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

        Box(
            Modifier
                .semantics { dialog() }
                .background(MaterialTheme.colorScheme.scrim)
                .fillMaxSize()
                .pointerInput(Unit) {}
                .anchoredDraggable(
                    state = draggableState,
                    enabled = model.imageCropper.cropState == null,
                    orientation = Orientation.Vertical
                )
        ) {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { model.closeImage() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                title = {
                    Text(stringResource(R.string.attach_gallery_photo))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .align(Alignment.TopStart)
            )

            val cropState = model.imageCropper.cropState
            if (cropState != null) {
                CropperPreview(cropState, Modifier.fillMaxSize())
                return@Box
            }

            AsyncImage(
                model = openedImage.file,
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .aspectRatio(
                        ratio = openedImage.width / openedImage.height.toFloat(),
                        matchHeightConstraintsFirst = true
                    )
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

            Row(
                Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)) {
                IconButton(onClick = { model.startImageCrop() }) {
                    Icon(
                        painter = painterResource(R.drawable.crop_24px),
                        contentDescription = stringResource(R.string.attach_gallery_crop)
                    )
                }
            }
        }
    }
}

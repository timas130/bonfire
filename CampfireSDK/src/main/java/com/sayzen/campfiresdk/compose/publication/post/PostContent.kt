package com.sayzen.campfiresdk.compose.publication.post

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.auth.SetBirthdayScreen
import com.sayzen.campfiresdk.compose.publication.post.PostModel.NsfwOverlayButtonVariant.*
import com.sayzen.campfiresdk.compose.publication.post.pages.PagesSource
import com.sayzen.campfiresdk.compose.publication.post.pages.PostPages
import com.sayzen.campfiresdk.compose.publication.post.pages.PostPagesModel
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast

@Composable
internal fun ExpandableColumn(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onExpandableChanged: (Boolean) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val spacedBy = with(LocalDensity.current) { 12.dp.roundToPx() }
    val clipHeight = with(LocalDensity.current) { 16.dp.toPx() }
    val maxHeight = with(LocalDensity.current) { 324.dp.roundToPx() }

    val colors = MaterialTheme.colorScheme

    val expandableRef = remember { Ref<Boolean>() }

    SubcomposeLayout(
        modifier
            .animateContentSize(
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    visibilityThreshold = IntSize.VisibilityThreshold,
                )
            )
            .clipToBounds()
            .drawWithContent {
                drawContent()
                if (expandableRef.value == true && !expanded) {
                    drawRect(
                        Brush.linearGradient(
                            colors = listOf(
                                colors.surfaceContainerLow.copy(alpha = 0f),
                                colors.surfaceContainerLow
                            ),
                            start = Offset(0f, size.height - clipHeight),
                            end = Offset(0f, size.height),
                        ),
                        topLeft = Offset(0f, size.height - clipHeight),
                        size = Size(size.width, clipHeight),
                    )
                }
            }
    ) { constraints ->
        val items = subcompose(Unit) {
            content()
        }

        var expandable = false
        val measurements = if (expanded) {
            onExpandableChanged(true)
            items.map { it.measure(constraints) }
        } else {
            val list = mutableListOf<Placeable>()
            var y = 0
            for (item in items) {
                val measurement = item.measure(constraints.copy(maxHeight = Constraints.Infinity))
                list.add(measurement)
                y += measurement.height + spacedBy

                if ((y - spacedBy) >= maxHeight) break
            }
            expandable = (y - spacedBy) >= maxHeight
            expandableRef.value = expandable
            onExpandableChanged(expandable)

            list
        }

        val height = if (expanded || !expandable) {
            measurements.sumOf { it.height + spacedBy } - spacedBy
        } else {
            maxHeight
        }
        layout(constraints.maxWidth, height.coerceAtLeast(0)) {
            var yPosition = 0
            for (measurement in measurements) {
                measurement.placeRelative(0, yPosition)
                yPosition += measurement.height + spacedBy
            }
        }
    }
}

@Composable
internal fun PostContent(
    post: PublicationPost,
    model: PostModel,
    onExpandableChanged: (Boolean) -> Unit,
    onExpand: () -> Unit,
) {
    val postPagesModel = viewModel {
        PostPagesModel(onExpand = onExpand)
    }

    val pagesSource = remember(post.id) {
        PagesSource(
            sourceType = API.PAGES_SOURCE_TYPE_POST,
            sourceId = post.id,
            sourceDateCreate = post.dateCreate,
        )
    }

    val expanded by model.expanded.collectAsState()
    val nsfwOverlayActive by model.nsfwOverlayActive.collectAsState()
    val nsfwOverlayButtons by model.nsfwOverlayButtons.collectAsState()

    val density = LocalDensity.current
    var contentHeightDp by remember { mutableFloatStateOf(150f) }

    Box(
        modifier = Modifier
            .onSizeChanged {
                contentHeightDp = with(density) { it.height.toDp().value }
            }
            .then(
                if (post.nsfw) {
                    Modifier.heightIn(min = 150.dp)
                } else {
                    Modifier
                }
            )
    ) {
        SelectionContainer {
            ExpandableColumn(
                modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && nsfwOverlayActive) {
                    // Modifier.blur is not supported on Android <12
                    Modifier
                        .blur(48.dp, BlurredEdgeTreatment.Unbounded)
                        .alpha(0.4f)
                } else if (nsfwOverlayActive) {
                    Modifier.alpha(0f)
                } else {
                    Modifier
                },
                expanded = expanded,
                onExpandableChanged = onExpandableChanged,
            ) {
                PostPages(
                    pages = post.pages.toList(),
                    source = pagesSource,
                    model = postPagesModel,
                )
            }
        }

        AnimatedVisibility(
            visible = nsfwOverlayActive,
            enter = scaleIn(initialScale = 0.9f) + fadeIn(),
            exit = scaleOut(targetScale = 0.9f) + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(contentHeightDp.fastCoerceAtMost(324f).dp)
                    .pointerInput(Unit) {},
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = stringResource(R.string.nsfw_title),
                    style = MaterialTheme.typography.titleMedium,
                )

                Row {
                    when (nsfwOverlayButtons) {
                        SpecifyAge -> {
                            TextButton(onClick = {
                                Navigator.to(SetBirthdayScreen())
                            }) {
                                Text(stringResource(R.string.nsfw_verify_age))
                            }

                            TextButton(onClick = {
                                ControllerSettings.showNsfwPosts = false
                                ToolsToast.show(R.string.hide_nsfw_posts_desc)
                            }) {
                                Text(stringResource(R.string.hide_nsfw_posts))
                            }
                        }
                        Open -> {
                            TextButton(onClick = {
                                model.dismissNsfw()
                            }) {
                                Text(stringResource(R.string.nsfw_open))
                            }
                        }
                        None -> {}
                    }
                }
            }
        }
    }
}

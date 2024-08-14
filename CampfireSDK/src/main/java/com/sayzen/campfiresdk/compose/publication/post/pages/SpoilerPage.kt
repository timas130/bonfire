package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.post.PageSpoiler
import com.sayzen.campfiresdk.R
import sh.sit.bonfire.formatting.compose.buildInlineAnnotatedString
import sh.sit.bonfire.formatting.core.BonfireFormatter

@Composable
internal fun PageSpoilerRenderer(
    page: PageSpoiler,
    model: PostPagesModel,
    source: PagesSource,
) {
    val expanded by model.isSpoilerExpanded(page.index).collectAsState()

    Column {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            leadingContent = {
                val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "SpoilerExpand")

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(
                        if (expanded) {
                            R.string.spoiler_shrink
                        } else {
                            R.string.spoiler_expand
                        }
                    ),
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = rotation
                        },
                )
            },
            headlineContent = {
                val colors = MaterialTheme.colorScheme
                val spoilerNameDefault = stringResource(R.string.spoiler_label_default)
                val spoilerName = remember(page.name, spoilerNameDefault, colors) {
                    val text = page.name?.takeUnless { it.isBlank() } ?: spoilerNameDefault
                    BonfireFormatter.parse(text, inlineOnly = true)
                        .buildInlineAnnotatedString(colors)
                }

                Text(
                    text = buildAnnotatedString {
                        append(spoilerName)
                        append(' ')
                        append(stringResource(R.string.spoiler_suffix, page.count))
                    }
                )
            },
            modifier = Modifier.clickable {
                if (expanded) {
                    model.shrinkSpoiler(page.index)
                } else {
                    model.expandSpoiler(page.index)
                }
            }
        )
        Column(
            Modifier
                .animateContentSize()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (expanded) {
                Spacer(Modifier.height(2.dp))
                page.children.forEachIndexed { index, child ->
                    PageMoveDestination(idx = index, source = source)
                    PostPage(page = child, model = model)
                }
                HorizontalDivider()
            }
        }
    }
}

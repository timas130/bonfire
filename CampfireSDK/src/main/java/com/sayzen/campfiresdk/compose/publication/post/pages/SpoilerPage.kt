package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.post.PageSpoiler
import com.sayzen.campfiresdk.R

@Composable
internal fun PageSpoilerRenderer(
    page: PageSpoiler,
    model: PostPagesModel,
    source: PagesSource,
) {
    val expanded by model.isSpoilerExpanded(page.index).collectAsState()

    ListItem(
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        leadingContent = {
            val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "SpoilerExpand")

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(if (expanded) {
                    R.string.spoiler_shrink
                } else {
                    R.string.spoiler_expand
                }),
                modifier = Modifier.rotate(rotation),
            )
        },
        headlineContent = {
            Text(
                text = stringResource(R.string.spoiler_label)
                    .format(
                        page.name?.takeUnless { it.isBlank() }
                            ?: stringResource(R.string.spoiler_label_default),
                        page.count
                    )
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (expanded) {
            page.children.forEachIndexed { index, child ->
                PageMoveDestination(idx = index, source = source)
                PostPage(page = child, model = model)
            }
            HorizontalDivider()
        }
    }
}

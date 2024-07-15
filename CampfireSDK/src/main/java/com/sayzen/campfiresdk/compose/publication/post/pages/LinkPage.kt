package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.post.PageLink
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsToast
import sh.sit.bonfire.formatting.compose.buildInlineAnnotatedString
import sh.sit.bonfire.formatting.core.BonfireFormatter

@Composable
internal fun PageLinkRenderer(page: PageLink) {
    PageLinkLayout(
        icon = {
            Icon(
                painterResource(R.drawable.ic_insert_link_white_24dp),
                stringResource(R.string.link)
            )
        },
        title = page.name,
        subtitle = page.link,
        link = page.link,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PageLinkLayout(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    link: String,
) {
    val colors = MaterialTheme.colorScheme
    val formattedTitle = remember(title, colors) {
        BonfireFormatter
            .parse(title, inlineOnly = true)
            .buildInlineAnnotatedString(colors)
    }

    Card(
        onClick = {
            ControllerLinks.openLink(link)
        },
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .combinedClickable(
                onClick = { ToolsIntent.openLink(link) },
                onLongClick = {
                    ToolsAndroid.setToClipboard(link)
                    ToolsToast.show(R.string.link_copied)
                }
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            icon()

            Column(Modifier.weight(1f)) {
                Text(
                    text = formattedTitle,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                )
                Text(
                    text = subtitle,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alpha(0.6f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun PageLinkPreview() {
    Surface {
        PageLinkRenderer(page = PageLink().apply {
            name = "This **is a link**"
            link = "https://bonfire.moe/a/very/long/url?with=many&parameters=also&blah=blah"
        })
    }
}

package com.sayzen.campfiresdk.compose.publication.post

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.posthog.PostHog
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.util.Avatar
import com.sayzen.campfiresdk.compose.util.IconButtonWithOffset
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.java.tools.ToolsDate
import sh.sit.bonfire.images.RemoteImage

@Composable
private fun DividerDot() {
    Box(
        Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    )
}

@Composable
internal fun PostHeader(post: PublicationPost) {
    var overflowsWidth by remember(
        post.fandom.name,
        post.creator.name,
        post.dateCreate
    ) {
        mutableStateOf(false)
    }

    val highlighted = post.important == API.PUBLICATION_IMPORTANT_IMPORTANT || post.isPined
    Surface(
        color = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        // sneaky!
        modifier = Modifier.padding(bottom = if (highlighted) 8.dp else 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Row(
                Modifier
                    .weight(1f, fill = false)
                    .padding(end = 6.dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)

                        if (!overflowsWidth) {
                            overflowsWidth = placeable.width >= constraints.maxWidth
                        }

                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(IntOffset.Zero)
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (PostHog.isFeatureEnabled("post_fandom_chip", true)) {
                    if (ControllerSettings.postFandomFirst) {
                        PostFandom(post)
                    } else {
                        PostCreator(post)
                    }
                    DividerDot()
                } else {
                    // fandom
                    PostFandom(post)
                    DividerDot()

                    // creator
                    Text(
                        text = "@${post.creator.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .then(if (overflowsWidth) Modifier.weight(2f, fill = false) else Modifier)
                            .clickable {
                                SProfile.instance(post.creator, Navigator.TO)
                            },
                    )
                    DividerDot()
                }

                // creation date
                val creationTimestamp = post.tag_4.takeIf { post.status == API.STATUS_PENDING } ?: post.dateCreate
                Text(
                    text = ToolsDate.dateToString(creationTimestamp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .then(if (overflowsWidth) Modifier.weight(1f, fill = false) else Modifier)
                        .clickable {
                            ToolsToast.show(ToolsDate.dateToString(creationTimestamp))
                        },
                )
            }

            val view = LocalView.current
            IconButtonWithOffset(onClick = { offset ->
                ControllerPost.getSplashMenu(post)
                    .asPopupShow(view, offset.x, offset.y)
            })
        }
    }
}

@Composable
private fun PostFandom(post: PublicationPost) {
    AnimatedContent(
        targetState = post.fandom,
        contentKey = { Pair(it.id, it.languageId) },
        label = "FandomChange"
    ) { fandom ->
        val shape = RoundedCornerShape((ControllerSettings.styleAvatarsRounding * (14f / 18f)).dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .padding(end = 6.dp)
                    .clip(shape)
                    .clickable {
                        SFandom.instance(fandom.id, fandom.languageId, Navigator.TO)
                    }
            ) {
                RemoteImage(
                    link = fandom.image,
                    contentDescription = fandom.name,
                    modifier = Modifier
                        .size(28.dp)
                )

                if (fandom.languageId != ControllerApi.getLanguageId()) {
                    val languageDrawable = ControllerApi.getDrawableForLanguage(fandom.languageId)

                    Image(
                        painter = painterResource(id = languageDrawable),
                        contentDescription = if (fandom.languageId == 0L) {
                            stringResource(R.string.multilingual_badge_alt)
                        } else {
                            ControllerApi.getLanguage(fandom.languageId).name
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(12.dp)
                            .clip(shape),
                    )
                }
            }

            Text(
                text = fandom.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable {
                        SFandom.instance(post.fandom, Navigator.TO)
                    },
            )
        }
    }
}

@Composable
private fun PostCreator(post: PublicationPost, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Avatar(
            account = post.creator,
            showLevel = false,
            modifier = Modifier
                .size(32.dp)
                .padding(end = 6.dp),
        )

        Text(
            text = post.creator.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .clickable {
                    SProfile.instance(post.creator, Navigator.TO)
                },
        )
    }
}

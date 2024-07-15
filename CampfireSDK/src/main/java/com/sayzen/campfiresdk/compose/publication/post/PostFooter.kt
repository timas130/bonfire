package com.sayzen.campfiresdk.compose.publication.post

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.posthog.PostHog
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.publication.KarmaCounter
import com.sayzen.campfiresdk.models.splashs.SplashComment
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sup.dev.android.libs.screens.navigator.Navigator

@Composable
internal fun PostFooter(
    post: PublicationPost,
    model: PostModel,
    expandable: Boolean,
    onExpand: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (expandable) {
            ExpandButton(onExpand, model)
        }
        Spacer(Modifier.weight(1f))

        if (post.isPublic) {
            CommentButton(post)
            KarmaCounter(post)
        }
    }
}

@Composable
internal fun ExpandButton(onExpand: (Boolean) -> Unit, model: PostModel) {
    val expanded by model.expanded.collectAsState()

    FilledTonalButton(
        onClick = {
            if (!expanded) {
                PostHog.capture("post_expand")
            }
            onExpand(!expanded)
        },
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = Modifier.height(36.dp)
    ) {
        val rotation by animateFloatAsState(
            targetValue = if (expanded) 180f else 0f,
            label = "ExpandedRotation",
        )

        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier
                .size(ButtonDefaults.IconSize)
                .rotate(rotation)
        )
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))

        AnimatedContent(targetState = expanded, label = "ExpandedText") {
            if (it) {
                Text(stringResource(R.string.post_shrink))
            } else {
                Text(stringResource(R.string.post_expand))
            }
        }
    }
}

val counterTransitionSpec: AnimatedContentTransitionScope<Long>.() -> ContentTransform = {
    if (targetState > initialState) {
        slideInVertically { it } + fadeIn() togetherWith
                slideOutVertically { -it } + fadeOut()
    } else {
        slideInVertically { -it } + fadeIn() togetherWith
                slideOutVertically { it } + fadeOut()
    }.using(
        SizeTransform(clip = true)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CommentButton(post: PublicationPost) {
    Surface(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = {
                    PostHog.capture("post_open", properties = mapOf("type" to "comments"))
                    SPost.instance(post.id, -1, Navigator.TO)
                },
                onLongClick = {
                    if (post.isPublic) {
                        PostHog.capture("open_comment_editor", properties = mapOf("from" to "post_longclick"))
                        SplashComment(post.id, null, true) { }.asSheetShow()
                    }
                },
            ),
        color = ButtonDefaults.filledTonalButtonColors().containerColor,
        contentColor = ButtonDefaults.filledTonalButtonColors().contentColor,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.comment_24),
                contentDescription = stringResource(R.string.post_comment),
                modifier = Modifier
                    .size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))

            AnimatedContent(
                targetState = post.subPublicationsCount,
                transitionSpec = counterTransitionSpec,
                label = "CommentCount"
            ) {
                Text(
                    text = it.toString(),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

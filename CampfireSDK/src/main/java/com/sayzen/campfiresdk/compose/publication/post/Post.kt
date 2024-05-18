package com.sayzen.campfiresdk.compose.publication.post

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.activities.user_activities.relay_race.SRelayRaceInfo
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.java.tools.ToolsDate
import kotlinx.coroutines.launch
import sh.sit.bonfire.auth.DecorFitsSystemWindowEffect
import sh.sit.bonfire.auth.components.BackButton
import sh.sit.bonfire.auth.components.RemoteImage

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
private fun PostHeader(
    post: PublicationPost,
) {
    Row(
        Modifier
            .padding(horizontal = 12.dp)
            .padding(top = 12.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RemoteImage(
            link = ImageLoader.load(post.fandom.image),
            contentDescription = post.fandom.name,
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape((ControllerSettings.styleAvatarsRounding * (14f / 18f)).dp))
                .clickable {
                    SFandom.instance(post.fandom.id, post.fandom.languageId, Navigator.TO)
                }
        )

        Text(
            text = post.fandom.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.clickable {
                SFandom.instance(post.fandom, Navigator.TO)
            }
        )
        DividerDot()
        Text(
            text = "@${post.creator.name}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.clickable {
                SProfile.instance(post.creator, Navigator.TO)
            }
        )
        DividerDot()
        Text(
            text = ToolsDate.dateToString(post.dateCreate),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Default.MoreVert, stringResource(R.string.post_more))
        }
    }
}

@Composable
private fun PostTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 8.dp),
    )
}

@Composable
private fun PostChips(post: PublicationPost) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            val activity = post.userActivity ?: return@item
            SuggestionChip(
                onClick = {
                    SRelayRaceInfo.instance(activity.id, Navigator.TO)
                },
                icon = {
                    Icon(painterResource(R.drawable.rowing_24), stringResource(R.string.post_activity))
                },
                label = {
                    Text(activity.name)
                }
            )
        }
    }
}

@Composable
private fun PostFooter(
    post: PublicationPost,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ExpandButton(onExpand, expanded)
        Spacer(Modifier.weight(1f))
        CommentButton(post)
    }
}

@Composable
private fun CommentButton(post: PublicationPost) {
    FilledTonalButton(
        onClick = {
            SPost.instance(post.id, -1, Navigator.TO)
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.comment_24),
            contentDescription = stringResource(R.string.post_comment),
            modifier = Modifier
                .size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))

        AnimatedContent(targetState = post.subPublicationsCount, label = "CommentCount") {
            Text(it.toString())
        }
    }
}

@Composable
private fun ExpandButton(onExpand: (Boolean) -> Unit, expanded: Boolean) {
    FilledTonalButton(
        onClick = {
            onExpand(!expanded)
        }
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

@Composable
private fun PostContent(expanded: Boolean) {
    SubcomposeLayout(
        Modifier
            .heightIn(max = if (expanded) Dp.Unspecified else 256.dp)
            .clipToBounds()
            .padding(horizontal = 12.dp)
            .animateContentSize()
    ) { constraints ->
        val items = subcompose(Unit) {
            val text = remember { LoremIpsum(1000).values.single().split(" ") }
            repeat(15) {
                Text(text.subList(it * 10, it * 10 + 10).joinToString(" "))
            }
        }

        val measurements = if (constraints.maxHeight == Constraints.Infinity) {
            items.map { it.measure(constraints) }
        } else {
            val list = mutableListOf<Placeable>()
            var y = 0
            for (item in items) {
                val measurement = item.measure(constraints)
                list.add(measurement)
                y += measurement.height

                if (y >= constraints.maxHeight) break
            }

            list
        }

        layout(constraints.maxWidth, measurements.sumOf { it.height }) {
            var yPosition = 0
            for (measurement in measurements) {
                measurement.placeRelative(0, yPosition)
                yPosition += measurement.height
            }
        }
    }
}

@Composable
fun Post(
    post: PublicationPost,
    scrollToTop: (() -> Unit)? = null,
    onClick: ((PublicationPost) -> Unit)? = null,
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Card(
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable(onClick != null) { onClick!!(post) }
    ) {
        PostHeader(post = post)

        PostTitle(title = post.title)

        PostChips(post = post)

        PostContent(expanded = expanded)

        PostFooter(
            post = post,
            expanded = expanded,
            onExpand = { expand ->
                if (!expand) {
                    scrollToTop?.invoke()
                }
                expanded = expand
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPostScreenC() {
    DecorFitsSystemWindowEffect()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton() },
                title = { Text(stringResource(R.string.post_comment)) }
            )
        },
    ) { paddingValues ->
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        LazyColumn(contentPadding = paddingValues, state = listState) {
            items(100) { index ->
                Post(
                    post = PublicationPost().apply {
                        title = "Это новый пост."
                        pages = arrayOf()
                        creator = ControllerApi.account.getAccount()
                        fandom = Fandom().apply {
                            name = "Minecraft"
                            image = ApiResources.AVATAR_2
                        }
                    },
                    scrollToTop = {
                        scope.launch {
                            val layoutInfo = listState.layoutInfo
                            val topVisible = layoutInfo.visibleItemsInfo
                                .find { it.index == index }
                                ?.let { it.offset > 0 } == true

                            if (!topVisible) {
                                listState.animateScrollToItem(index)
                            }
                        }
                    }
                )
            }
        }
    }
}

class TestPostScreen : ComposeScreen() {
    @Composable
    override fun Content() {
        TestPostScreenC()
    }
}

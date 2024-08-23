package com.sayzen.campfiresdk.compose.attach

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.Query
import com.sayzen.campfiresdk.FavouriteGifsQuery
import com.sayzen.campfiresdk.GifSearchQuery
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.util.InfiniteListHandler
import com.sayzen.campfiresdk.compose.util.RemoteImageShimmer
import com.sayzen.campfiresdk.compose.util.pagination.AbstractGQLPaginationModel
import com.sayzen.campfiresdk.compose.util.shimmerExt
import com.sayzen.campfiresdk.fragment.AttachGifItem
import com.sayzen.campfiresdk.fragment.FavouriteGifs
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import sh.sit.bonfire.images.RemoteImage
import sh.sit.bonfire.images.toRef

internal class GifFavouritesModel(application: Application) :
    AbstractGQLPaginationModel<FavouriteGifs.Edge, FavouriteGifsQuery.Data>(application) {

    private var started: Boolean = false
    fun start() {
        if (started) return
        started = true

        reload()
    }

    override fun hasNextPage(data: FavouriteGifsQuery.Data): Boolean {
        return data.me.favouriteGifs.favouriteGifs.pageInfo.hasNextPage
    }

    override fun toItems(data: FavouriteGifsQuery.Data): List<FavouriteGifs.Edge> {
        return data.me.favouriteGifs.favouriteGifs.edges
    }

    override fun createQuery(items: List<FavouriteGifs.Edge>): Query<FavouriteGifsQuery.Data> {
        return FavouriteGifsQuery(Optional.presentIfNotNull(items.lastOrNull()?.cursor))
    }
}

@OptIn(FlowPreview::class)
internal class GifSearchModel(application: Application, private val term: StateFlow<String>) :
    AbstractGQLPaginationModel<GifSearchQuery.Edge, GifSearchQuery.Data>(application) {

    private var started: Boolean = false
    fun start() {
        if (started) return
        started = true

        viewModelScope.launch {
            term.sample(500)
                .distinctUntilChanged()
                .collect { reload() }
        }
    }

    override fun hasNextPage(data: GifSearchQuery.Data): Boolean {
        return data.searchGif.pageInfo.hasNextPage
    }

    override fun toItems(data: GifSearchQuery.Data): List<GifSearchQuery.Edge> {
        return data.searchGif.edges
    }

    override fun createQuery(items: List<GifSearchQuery.Edge>): Query<GifSearchQuery.Data> {
        return GifSearchQuery(term.value, Optional.presentIfNotNull(items.lastOrNull()?.cursor))
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun GifTab(model: AttachFlyoutModel, sharedTransitionScope: SharedTransitionScope) {
    val recentGifs by model.recentGifs.collectAsState()

    val favouriteGifs by model.gifFavouritesModel.items.collectAsState()

    val searchResults by model.gifSearchModel.items.collectAsState()
    val searchQuery by model.gifQuery.collectAsState()

    val chunkedRecentGifs by derivedStateOf {
        recentGifs?.chunked(3)
    }
    val chunkedFavouriteGifs by derivedStateOf {
        favouriteGifs?.chunked(3)
    }
    val chunkedSearchResults by derivedStateOf {
        searchResults?.chunked(3) ?: emptyList()
    }

    val listState = rememberLazyListState()
    val shimmer = rememberShimmer(ShimmerBounds.Window)

    LaunchedEffect(searchQuery) {
        listState.requestScrollToItem(0)
    }

    InfiniteListHandler(listState, buffer = 4 + 2) {
        model.gifSearchModel.loadNextPage()
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = 4.dp, end = 4.dp, bottom = (132 + 8).dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (chunkedRecentGifs?.isEmpty() != true && searchQuery.isBlank()) {
            item(key = "attach_scroll_recent") {
                AttachTabSubtitle {
                    Icon(painterResource(R.drawable.history_20px), null)

                    Text(stringResource(R.string.attach_scroll_recent))
                }
            }

            items(chunkedRecentGifs ?: emptyList(), key = { "recent:" + it.first().id }) { chunk ->
                GifsChunk(
                    parentKey = "recent",
                    chunk = chunk,
                    model = model,
                    shimmer = shimmer,
                    sharedTransitionScope = sharedTransitionScope
                )
            }
            if (chunkedRecentGifs == null) {
                items(3) {
                    ShimmerGifsChunk(shimmer)
                }
            }
        }

        if (chunkedFavouriteGifs?.isEmpty() != true && searchQuery.isBlank()) {
            item(key = "attach_scroll_favourites") {
                AttachTabSubtitle {
                    Icon(painterResource(R.drawable.favorite_20px), null)

                    Text(stringResource(R.string.attach_scroll_favourite))
                }
            }

            items(chunkedFavouriteGifs ?: emptyList(), key = { "favourite:" + it.first().node.id }) { chunk ->
                GifsChunk(
                    parentKey = "favourite",
                    chunk = chunk.map { it.node.attachGifItem },
                    model = model,
                    shimmer = shimmer,
                    sharedTransitionScope = sharedTransitionScope
                )
            }
            items(4) { idx ->
                if (idx == 0) {
                    LaunchedEffect(Unit) {
                        model.gifFavouritesModel.loadNextPage()
                    }
                }

                ShimmerGifsChunk(shimmer)
            }
        }

        item(key = "attach_scroll_search") {
            AnimatedContent(searchQuery.isBlank(), label = "SearchTrendingLabel") { isTrending ->
                if (isTrending) {
                    AttachTabSubtitle {
                        Icon(painterResource(R.drawable.trending_up_20px), null)

                        Text(stringResource(R.string.attach_scroll_trending))
                    }
                } else {
                    AttachTabSubtitle {
                        Icon(painterResource(R.drawable.search_20px), null)

                        Text(stringResource(R.string.attach_scroll_search))
                    }
                }
            }
        }

        items(chunkedSearchResults, key = { "search:" + it.first().node.id }) { chunk ->
            GifsChunk(
                parentKey = "search",
                chunk = chunk.map { it.node.attachGifItem },
                model = model,
                shimmer = shimmer,
                sharedTransitionScope = sharedTransitionScope
            )
        }
        items(4) {
            ShimmerGifsChunk(shimmer)
        }
    }
}

@Composable
private fun ShimmerGifsChunk(shimmer: Shimmer) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) {
            Box(
                Modifier
                    .shimmerExt(true, shimmer)
                    .weight(1f)
                    .height(150.dp)
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GifsChunk(
    parentKey: String,
    chunk: List<AttachGifItem>,
    model: AttachFlyoutModel,
    shimmer: Shimmer,
    sharedTransitionScope: SharedTransitionScope,
) {
    val activeGifPopup by model.activeGifPopup.collectAsState()

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        chunk.forEach { gif ->
            with(sharedTransitionScope) {
                val isActivePopup by derivedStateOf {
                    activeGifPopup?.second?.id == gif.id
                }

                GifItem(
                    parentKey = parentKey,
                    model = model,
                    gif = gif,
                    shimmer = shimmer,
                    modifier = Modifier
                        .weight(gif.media.preview.w!!.toFloat())
                        .sharedElementWithCallerManagedVisibility(
                            rememberSharedContentState("$parentKey:${gif.id}"),
                            !isActivePopup,
                        )
                )
            }
        }

        repeat(3 - chunk.size) {
            val averageWidth = chunk.sumOf { it.media.preview.w!! } / chunk.size.toFloat()

            Box(Modifier.weight(averageWidth))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GifItem(
    parentKey: String,
    model: AttachFlyoutModel,
    gif: AttachGifItem,
    shimmer: Shimmer,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    RemoteImage(
        link = gif.media.tinyGif.ui.toRef(),
        loader = { RemoteImageShimmer(shimmer) },
        contentDescription = "",
        contentScale = ContentScale.Crop,
        forceAspectRatio = false,
        modifier = modifier
            .height(150.dp)
            .combinedClickable(
                onClick = {
                    model.shareGif(gif)
                },
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    model.openGifPopup(parentKey, gif)
                }
            )
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun BoxScope.AttachGifPopup(
    activePopup: Pair<String, AttachGifItem>,
    model: AttachFlyoutModel,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier
) {
    val (parentKey, gif) = activePopup

    // we have to check if the popup should still be fully visible
    // or whether it's in the process of fading out inside
    // the wrapping AnimatedVisibility.
    // that's also the reason activePopup is passed as an argument.
    val activePopupUpd by model.activeGifPopup.collectAsState()
    val popupStillActive by derivedStateOf {
        activePopupUpd != null
    }

    BackHandler(popupStillActive) {
        model.closeGifPopup()
    }

    Column(
        modifier = modifier
            .matchParentSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
            .clickable(interactionSource = null, indication = null) {
                model.closeGifPopup()
            },
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        with(sharedTransitionScope) {
            RemoteImage(
                link = gif.media.tinyGif.ui.toRef(),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                forceAspectRatio = true,
                matchHeightConstraintsFirst = true,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .sharedElementWithCallerManagedVisibility(
                        rememberSharedContentState(key = "$parentKey:${gif.id}"),
                        popupStillActive,
                    )
                    .heightIn(max = 400.dp)
            )
        }
    }
}

@Composable
internal fun AttachTabSubtitle(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .height(28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleSmall) {
            content()
        }
    }
}

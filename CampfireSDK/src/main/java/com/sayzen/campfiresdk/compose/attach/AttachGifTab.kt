package com.sayzen.campfiresdk.compose.attach

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.Query
import com.sayzen.campfiresdk.GifSearchQuery
import com.sayzen.campfiresdk.compose.util.InfiniteListHandler
import com.sayzen.campfiresdk.compose.util.RemoteImageShimmer
import com.sayzen.campfiresdk.compose.util.pagination.AbstractGQLPaginationModel
import com.sayzen.campfiresdk.compose.util.shimmerExt
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

@Composable
internal fun GifTab(model: AttachFlyoutModel) {
    val searchResults by model.gifSearchModel.items.collectAsState()
    val searchQuery by model.gifQuery.collectAsState()

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
        item(key = "scroll_start") {
            Box {}
        }

        items(chunkedSearchResults, key = { it.first().node.id }) { chunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                chunk.forEach { gif ->
                    GifItem(
                        model = model,
                        gif = gif.node,
                        shimmer = shimmer,
                        modifier = Modifier.weight(gif.node.media.preview.w!!.toFloat())
                    )
                }
            }
        }
        items(4) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) {
                    Box(Modifier
                        .shimmerExt(true, shimmer)
                        .weight(1f)
                        .height(150.dp))
                }
            }
        }
    }
}

@Composable
private fun GifItem(
    model: AttachFlyoutModel,
    gif: GifSearchQuery.Node,
    shimmer: Shimmer,
    modifier: Modifier = Modifier
) {
    RemoteImage(
        link = gif.media.tinyGif.ui.toRef(),
        loader = { RemoteImageShimmer() },
        contentDescription = "",
        contentScale = ContentScale.Crop,
        forceAspectRatio = false,
        modifier = modifier.height(150.dp)
    )

//    val context = LocalContext.current
//
//    val player = remember {
//        ExoPlayer.Builder(context)
//            .build()
//    }
//
//    LaunchedEffect(gif.id) {
//        player.setMediaItem(MediaItem.fromUri(gif.media.tinyMp4.u))
//        player.prepare()
//        player.repeatMode = Player.REPEAT_MODE_ONE
//        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
//        player.playWhenReady = true
//    }
//
//    AndroidView(
//        factory = {
//            val view = SurfaceView(it)
//            player.setVideoSurfaceView(view)
//            view
//        },
//        modifier = modifier
//            .aspectRatio(1f)
//    )
//
//    DisposableEffect(Unit) {
//        onDispose { player.release() }
//    }
}

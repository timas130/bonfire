package com.sayzen.campfiresdk.compose.util.pagination

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.util.EmptyCard
import com.sayzen.campfiresdk.compose.util.ErrorCard
import com.sayzen.campfiresdk.compose.util.InfiniteListHandler
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import sh.sit.bonfire.auth.DecorFitsSystemWindowEffect
import sh.sit.bonfire.auth.components.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PaginationScreen(
    model: PaginationModel<T>,
    topContent: @Composable () -> Unit = {},
    title: @Composable () -> Unit = {},
    errorCard: @Composable () -> Unit = {
        ErrorCard(text = stringResource(R.string.list_error_generic))
    },
    emptyCard: @Composable () -> Unit = {
        EmptyCard(text = stringResource(R.string.list_empty_generic))
    },
    isRefreshable: Boolean = true,
    snackbarHostState: SnackbarHostState? = null,
    // the outer should have onChoose already, so not including it here
    item: @Composable (item: T?, shimmer: Shimmer) -> Unit,
) {
    val isRefreshableState by rememberUpdatedState(isRefreshable)

    val isError by model.isError.collectAsState()
    val isLoading by model.isLoading.collectAsState()
    val items by model.items.collectAsState()
    val hasMore by model.hasMore.collectAsState()

    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    val pullToRefresh = rememberPullToRefreshState()

    DecorFitsSystemWindowEffect()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton() },
                title = title,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            if (snackbarHostState != null) {
                SnackbarHost(snackbarHostState)
            }
        },
        modifier = Modifier
            .pullToRefresh(
                enabled = isRefreshableState,
                isRefreshing = isLoading,
                state = pullToRefresh,
                onRefresh = { model.reload() }
            )
    ) { contentPadding ->
        val listState = rememberLazyListState()

        LazyColumn(
            contentPadding = contentPadding,
            state = listState,
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                topContent()
            }

            items(items ?: emptyList()) {
                item(it, shimmer)
            }

            item {
                AnimatedVisibility(visible = isError) {
                    errorCard()
                }
            }
            item {
                if (items?.isEmpty() == true) {
                    emptyCard()
                }
            }

            items(4) {
                if (hasMore != false && !isError) {
                    item(null, shimmer)
                }
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            PullToRefreshDefaults.Indicator(
                state = pullToRefresh,
                isRefreshing = isLoading,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        InfiniteListHandler(listState = listState, buffer = 4 + 2) {
            model.loadNextPage()
        }
    }
}

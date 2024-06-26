package com.sayzen.campfiresdk.compose.profile.badges.list

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.compose.util.EmptyCard
import com.sayzen.campfiresdk.compose.util.ErrorCard
import com.sayzen.campfiresdk.compose.util.InfiniteListHandler
import com.sayzen.campfiresdk.fragment.BadgeListItem
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import sh.sit.bonfire.auth.DecorFitsSystemWindowEffect
import sh.sit.bonfire.auth.components.BackButton

class BadgeListScreen(
    private val userId: String,
    private val onChoose: ((BadgeListItem?) -> Unit)? = null,
) : ComposeScreen() {
    @Composable
    override fun Content() {
        BadgeList(userId, onChoose)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeList(userId: String, onChoose: ((BadgeListItem?) -> Unit)?) {
    val model = viewModel<BadgeListScreenModel>(key = "BadgeListScreenModel:$userId") {
        BadgeListScreenModel(get(APPLICATION_KEY)!!, userId)
    }

    val isError by model.isError.collectAsState()
    val isLoading by model.isLoading.collectAsState()
    val badges by model.badges.collectAsState()
    val hasMore by model.hasMore.collectAsState()

    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    val pullToRefresh = rememberPullToRefreshState()

    DecorFitsSystemWindowEffect()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton() },
                title = {
                    if (onChoose == null) {
                        Text(stringResource(R.string.badge_list))
                    } else {
                        Text(stringResource(R.string.badge_choose))
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .pullToRefresh(
                isRefreshing = isLoading,
                state = pullToRefresh,
                onRefresh = { model.reload() }
            )
    ) { contentPadding ->
        val listState = rememberLazyListState()

        LazyColumn(
            contentPadding = contentPadding,
            state = listState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                if (onChoose != null) {
                    ListItem(
                        headlineContent = {
                            Text(stringResource(R.string.badge_list_remove))
                        },
                        modifier = Modifier.clickable {
                            Navigator.back()
                            onChoose(null)
                        }
                    )
                }
            }
            items(badges ?: emptyList()) {
                BadgeListItem(it.node.badgeListItem, shimmer, onChoose)
            }

            item {
                if (isError) {
                    ErrorCard(text = stringResource(R.string.badge_list_error))
                }
            }
            item {
                if (badges?.isEmpty() == true) {
                    EmptyCard(text = stringResource(R.string.badge_list_empty))
                }
            }

            items(arrayOfNulls<Unit?>(4)) {
                if (hasMore != false && !isError) { // if null or true
                    BadgeListItem(null, shimmer, onChoose)
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
            model.nextPage()
        }
    }
}

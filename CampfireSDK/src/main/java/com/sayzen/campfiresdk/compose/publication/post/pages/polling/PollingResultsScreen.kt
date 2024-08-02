package com.sayzen.campfiresdk.compose.publication.post.pages.polling

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dzen.campfire.api.models.publications.post.PagePolling
import com.dzen.campfire.api.requests.post.RPostPagePollingGetVotes
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.compose.publication.post.pages.PagesSource
import com.sayzen.campfiresdk.compose.util.*
import com.sayzen.campfiresdk.compose.util.pagination.AbstractLegacyPaginationModel
import com.sayzen.campfiresdk.compose.util.pagination.PaginationScreen
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.support.ApiRequestsSupporter.sendSuspendExt
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.java.tools.ToolsDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

typealias PollResult = RPostPagePollingGetVotes.PollingResultsItem

class PollingResultsModel(
    private val pagesSource: PagesSource,
    private val pollingId: Long,
) : AbstractLegacyPaginationModel<PollResult>() {
    init {
        reload()
    }

    private val _pollItemFilter = MutableStateFlow<Long?>(null)
    val pollItemFilter = _pollItemFilter.asStateFlow()

    fun setFilter(itemId: Long?) {
        _pollItemFilter.value = itemId
    }

    // omg this is so clean
    override val items: StateFlow<List<PollResult>?>
        get() = super.items.combineStates(pollItemFilter) { items, filter ->
            if (filter != null) {
                items?.fastFilter { it.itemId == filter }
            } else {
                items
            }
        }

    override suspend fun load(current: List<PollResult>): List<PollResult> {
        return RPostPagePollingGetVotes(
            sourceType = pagesSource.sourceType,
            sourceId = pagesSource.sourceId,
            sourceIdSub = pagesSource.sourceSubId,
            pollingId = pollingId,
            offset = current.size,
        )
        .sendSuspendExt()
        .results
        .toList()
    }
}

@Composable
private fun PollingResultsScreenC(
    page: PagePolling,
    pagesSource: PagesSource,
) {
    val model = viewModel(key = "PollingResultsModel:${page.pollingId}") {
        PollingResultsModel(pagesSource, page.pollingId)
    }

    PaginationScreen(
        model = model,
        title = {
            Text(stringResource(R.string.poll_results_title))
        },
        errorCard = {
            ErrorCard(text = stringResource(R.string.poll_results_error))
        },
        emptyCard = {
            EmptyCard(text = stringResource(R.string.poll_results_empty))
        },
        topContent = {
            val pollItemFilter by model.pollItemFilter.collectAsState()

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(page.options) { itemId, option ->
                    FilterChip(
                        selected = pollItemFilter == itemId.toLong(),
                        onClick = {
                            if (pollItemFilter == itemId.toLong()) {
                                model.setFilter(null)
                            } else {
                                model.setFilter(itemId.toLong())
                            }
                        },
                        leadingIcon = {
                            Row {
                                AnimatedVisibility(visible = pollItemFilter == itemId.toLong()) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            }
                        },
                        label = {
                            Text(option, maxLines = 1)
                        }
                    )
                }
            }
        },
        item = { item, shimmer ->
            ListItem(
                headlineContent = {
                    Text(
                        item?.account?.name ?: ".............",
                        Modifier.shimmerExt(item == null, shimmer),
                    )
                },
                overlineContent = {
                    Text(
                        item?.timestamp?.let { ToolsDate.dateToString(it) } ?: ".......",
                        Modifier.shimmerExt(item == null, shimmer),
                    )
                },
                supportingContent = {
                    Text(
                        item?.itemId?.let { page.options[it.toInt()] } ?: "...........",
                        Modifier.shimmerExt(item == null, shimmer),
                    )
                },
                leadingContent = {
                    if (item != null) {
                        Avatar(account = item.account)
                    } else {
                        AvatarShimmer(shimmer = shimmer)
                    }
                },
                modifier = Modifier
                    .clickable(enabled = item != null) {
                        SProfile.instance(item!!.account, Navigator.TO)
                    }
            )
        }
    )
}

class PollingResultsScreen(
    private val page: PagePolling,
    private val pagesSource: PagesSource,
) : ComposeScreen() {
    @Composable
    override fun Content() {
        PollingResultsScreenC(page = page, pagesSource = pagesSource)
    }
}

package com.sayzen.campfiresdk.compose.publication.post.pages.polling

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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

typealias PollResult = RPostPagePollingGetVotes.PollingResultsItem

class PollingResultsModel(
    private val pagesSource: PagesSource,
    private val pollingId: Long,
) : AbstractLegacyPaginationModel<PollResult>() {
    init {
        reload()
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

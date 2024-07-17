package com.sayzen.campfiresdk.compose.publication.post.pages.polling

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.publications.post.PagePolling
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.compose.util.Avatar
import com.sayzen.campfiresdk.compose.util.AvatarShimmer
import com.sayzen.campfiresdk.compose.util.pagination.AbstractLegacyPaginationModel
import com.sayzen.campfiresdk.compose.util.pagination.PaginationScreen
import com.sayzen.campfiresdk.compose.util.shimmerExt

class PollingBlacklistModel(private val page: PagePolling) : AbstractLegacyPaginationModel<Account>() {
    override suspend fun load(current: List<Account>): List<Account> {
        if (current.isNotEmpty()) return emptyList()
        return page.blacklist.toList()
    }
}

class PollingBlacklistScreen(val page: PagePolling) : ComposeScreen() {
    @Composable
    override fun Content() {
        val model = viewModel {
            PollingBlacklistModel(page)
        }

        PaginationScreen(
            model = model,
            title = {
                Text(stringResource(R.string.poll_limits_blacklist))
            },
            isRefreshable = false,
        ) { account, shimmer ->
            ListItem(
                headlineContent = {
                    Text(
                        account?.name ?: "..............",
                        Modifier.shimmerExt(account == null, shimmer),
                    )
                },
                leadingContent = {
                    if (account != null) {
                        Avatar(account = account)
                    } else {
                        AvatarShimmer(shimmer = shimmer)
                    }
                }
            )
        }
    }
}

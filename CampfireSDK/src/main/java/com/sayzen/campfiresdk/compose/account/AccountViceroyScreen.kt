package com.sayzen.campfiresdk.compose.account

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAllViceroy
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.compose.fandom.FandomCard
import com.sayzen.campfiresdk.compose.util.EmptyCard
import com.sayzen.campfiresdk.compose.util.ErrorCard
import com.sayzen.campfiresdk.compose.util.pagination.AbstractLegacyPaginationModel
import com.sayzen.campfiresdk.compose.util.pagination.PaginationScreen
import com.sayzen.campfiresdk.support.ApiRequestsSupporter.sendSuspendExt

class AccountViceroyScreenModel(
    private val accountId: Long,
) : AbstractLegacyPaginationModel<Fandom>() {
    init {
        reload()
    }

    override suspend fun load(current: List<Fandom>): List<Fandom> {
        return RFandomsGetAllViceroy(
            accountId = accountId,
            offset = current.size.toLong(),
        )
        .sendSuspendExt()
        .fandoms
        .toList()
    }
}

@Composable
private fun AccountViceroyScreenC(
    accountId: Long,
) {
    val model = viewModel(key = "AccountViceroyScreen:$accountId") {
        AccountViceroyScreenModel(accountId)
    }

    PaginationScreen(
        model = model,
        title = {
            Text(stringResource(R.string.account_viceroy_list))
        },
        errorCard = {
            ErrorCard(text = stringResource(R.string.account_viceroy_list_error))
        },
        emptyCard = {
            EmptyCard(text = stringResource(R.string.account_viceroy_list_empty))
        },
        item = { item, shimmer ->
            FandomCard(item, shimmer)
        },
    )
}

class AccountViceroyScreen(private val accountId: Long) : ComposeScreen() {
    @Composable
    override fun Content() {
        AccountViceroyScreenC(accountId)
    }
}

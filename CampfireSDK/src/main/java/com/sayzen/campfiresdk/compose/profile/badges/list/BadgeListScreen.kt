package com.sayzen.campfiresdk.compose.profile.badges.list

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.compose.util.EmptyCard
import com.sayzen.campfiresdk.compose.util.ErrorCard
import com.sayzen.campfiresdk.compose.util.pagination.PaginationScreen
import com.sayzen.campfiresdk.fragment.BadgeListItem
import com.sup.dev.android.libs.screens.navigator.Navigator

class BadgeListScreen(
    private val userId: String,
    private val onChoose: ((BadgeListItem?) -> Unit)? = null,
) : ComposeScreen() {
    @Composable
    override fun Content() {
        BadgeList(userId, onChoose)
    }
}

@Composable
fun BadgeList(userId: String, onChoose: ((BadgeListItem?) -> Unit)? = null) {
    val model = viewModel<BadgeListScreenModel>(key = "BadgeListScreenModel:$userId") {
        BadgeListScreenModel(get(APPLICATION_KEY)!!, userId)
    }

    PaginationScreen(
        model = model,
        topContent = {
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
        },
        title = {
            if (onChoose == null) {
                Text(stringResource(R.string.badge_list))
            } else {
                Text(stringResource(R.string.badge_choose))
            }
        },
        errorCard = {
            ErrorCard(text = stringResource(R.string.badge_list_error))
        },
        emptyCard = {
            EmptyCard(text = stringResource(R.string.badge_list_empty))
        },
        item = { item, shimmer ->
            BadgeListItem(
                item = item?.node?.badgeListItem,
                shimmer = shimmer,
                onChoose = onChoose,
            )
        }
    )
}

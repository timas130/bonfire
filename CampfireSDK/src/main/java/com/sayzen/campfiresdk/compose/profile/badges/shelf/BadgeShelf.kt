package com.sayzen.campfiresdk.compose.profile.badges.shelf

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apollographql.apollo3.api.Optional
import com.sayzen.campfiresdk.BadgeShelfQuery
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeCard
import com.sayzen.campfiresdk.compose.profile.ui.CompatProfileCard
import com.sayzen.campfiresdk.compose.util.ErrorCard
import com.sup.dev.android.tools.ToolsResources
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import kotlinx.coroutines.flow.map

@Composable
private fun BadgeShelf(
    model: BadgeShelfModel,
    shelf: List<BadgeShelfQuery.BadgeShelf?>?,
) {
    val isEditingAllowed by model.isEditingAllowed.collectAsState(initial = false)

    CompatProfileCard {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.badge_shelf),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )

            if (isEditingAllowed) {
                IconButton(
                    onClick = { model.hide() },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.eye_off),
                        contentDescription = stringResource(R.string.badge_shelf_hide),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            IconButton(
                onClick = { model.toList() },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.badge_shelf_more),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        ) {
            (shelf ?: emptyList()).forEachIndexed { index, item ->
                BadgeIcon(
                    badgeIcon = item?.badgeShelfIcon,
                    model = model,
                    index = index,
                )
            }

            if (shelf == null) {
                repeat(4) { idx ->
                    BadgeIcon(
                        badgeIcon = null,
                        model = model,
                        index = idx,
                        shimmer = shimmer,
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeShelfWrapper(userId: String) {
    val model = viewModel<BadgeShelfModel>(factory = BadgeShelfModelFactory(userId))

    val shelf = model.shelf
        .map { Optional.presentIfNotNull(it) }
        .collectAsState(initial = null).value
    val isError by model.isError.collectAsState(initial = false)
    val isVisible by model.isVisible.collectAsState(initial = true)
    val isShowButtonVisible by model.isShowButtonVisible.collectAsState(initial = false)

    AnimatedVisibility(visible = isVisible) {
        BadgeShelf(model, shelf?.getOrNull())
    }
    AnimatedVisibility(visible = isShowButtonVisible) {
        val isLoadingShow by model.isLoadingShow.collectAsState(initial = true)
        val context = LocalContext.current

        CompatProfileCard {
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.badge_shelf_show))
                },
                trailingContent = {
                    AnimatedVisibility(visible = isLoadingShow) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color(remember { ToolsResources.getColorAttr(context, R.attr.colorSurface) }),
                ),
                modifier = Modifier.clickable {
                    model.show()
                }
            )
        }
    }
    AnimatedVisibility(visible = isError) {
        ErrorCard(text = stringResource(R.string.badge_shelf_error))
    }
}

class BadgeShelfCard(
    private val userId: String,
) : ComposeCard() {
    @Composable
    override fun Content() {
        BadgeShelfWrapper(userId = userId)
    }

    @Composable
    override fun getBackground(): Color {
        return Color.Transparent
    }
}

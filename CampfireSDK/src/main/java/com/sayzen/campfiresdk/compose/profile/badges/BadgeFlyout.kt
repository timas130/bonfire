package com.sayzen.campfiresdk.compose.profile.badges

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apollographql.apollo3.api.ApolloResponse
import com.dzen.campfire.api.models.account.AccountBadge
import com.posthog.PostHog
import com.sayzen.campfiresdk.BadgeFlyoutQuery
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeSplash
import com.sayzen.campfiresdk.compose.util.ErrorCard
import com.sayzen.campfiresdk.compose.util.shimmerExt
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsIntent
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import sh.sit.bonfire.auth.ApolloController
import sh.sit.bonfire.auth.components.BetterModalBottomSheet
import sh.sit.bonfire.auth.watchExt
import sh.sit.bonfire.images.RemoteImage
import sh.sit.bonfire.images.toRef

class BadgeFlyoutSplash(private val shortBadge: AccountBadge) : ComposeSplash() {
    private val isShownFlow = MutableStateFlow(isShown())

    init {
        noBackground = true
    }

    @Composable
    override fun Content() {
        BadgeFlyout(
            open = isShownFlow.collectAsState().value,
            close = {
                hide()
            },
            shortBadge = shortBadge,
        )
    }

    override fun onShow() {
        super.onShow()
        isShownFlow.tryEmit(isShown())
    }

    override fun onHide() {
        super.onHide()
        isShownFlow.tryEmit(isShown())
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeFlyout(
    open: Boolean,
    close: () -> Unit,
    shortBadge: AccountBadge,
) {
    val context = LocalContext.current

    var query by remember { mutableStateOf<ApolloResponse<BadgeFlyoutQuery.Data>?>(null) }

    LaunchedEffect(open) {
        if (!open) return@LaunchedEffect

        PostHog.capture("badge flyout viewed", properties = mapOf(
            "badge_id" to shortBadge.id,
        ))

        withContext(Dispatchers.IO) {
            ApolloController.apolloClient
                .query(BadgeFlyoutQuery(shortBadge.id.toString()))
                .watchExt(context)
                .collect { query = it }
        }
    }

    val isError = !query?.errors.isNullOrEmpty()
    val fullBadge = query?.data?.badge

    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    BetterModalBottomSheet(open = open, onDismissRequest = close) {
        RemoteImage(
            link = fullBadge?.image?.ui?.toRef() ?: shortBadge.miniImage,
            contentDescription = stringResource(R.string.badge_alt),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(100.dp)
                .padding(vertical = 8.dp),
        )

        AnimatedVisibility(visible = isError, Modifier.align(Alignment.CenterHorizontally)) {
            ErrorCard(text = stringResource(R.string.badge_error))
        }

        AnimatedVisibility(visible = !isError) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = fullBadge?.name ?: "Loading",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp)
                        .shimmerExt(fullBadge == null, shimmer)
                        .animateContentSize(),
                )

                Text(
                    text = fullBadge?.description ?: "Loading Loading Loading Loading Loading Loading",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shimmerExt(fullBadge == null, shimmer)
                        .animateContentSize()
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    AnimatedVisibility(visible = fullBadge == null || fullBadge.link != null) {
                        Button(
                            onClick = { ToolsIntent.openLink(fullBadge?.link!!) },
                            enabled = fullBadge != null,
                        ) {
                            Text(stringResource(R.string.badge_more))
                        }
                    }

                    AnimatedVisibility(visible = fullBadge == null || fullBadge.fandomId != null) {
                        OutlinedButton(
                            onClick = {
                                SFandom.instance(fullBadge?.fandomId?.toLong() ?: 0, Navigator.TO)
                            },
                            enabled = fullBadge != null,
                        ) {
                            Text(stringResource(R.string.badge_fandom))
                        }
                    }
                }
            }
        }
    }
}

package com.sayzen.campfiresdk.compose.fandom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dzen.campfire.api.models.fandoms.Fandom
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.accentNegative
import com.sayzen.campfiresdk.compose.accentPositive
import com.sayzen.campfiresdk.compose.util.Avatar
import com.sayzen.campfiresdk.compose.util.AvatarShimmer
import com.sayzen.campfiresdk.compose.util.shimmerExt
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.valentinilk.shimmer.Shimmer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FandomCard(initialFandom: Fandom?, shimmer: Shimmer? = null, onClick: ((Fandom) -> Unit)? = null) {
    val dataSource = remember(initialFandom?.id) { initialFandom?.let { FandomDataSource(it) } }
    val fandom = dataSource?.flow?.collectAsState()?.value ?: initialFandom

    ListItem(
        // avatar
        leadingContent = {
            if (fandom != null) {
                Avatar(fandom)
            } else {
                AvatarShimmer(shimmer)
            }
        },
        // fandom title
        headlineContent = {
            Text(
                text = fandom?.name ?: ".................",
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.shimmerExt(fandom == null, shimmer)
            )
        },
        // subscriber count
        supportingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.fandom_subscribers_alt),
                    modifier = Modifier
                        .size(20.dp)
                )

                Text(
                    fandom?.subscribesCount?.toString() ?: ".....",
                    Modifier.shimmerExt(fandom == null, shimmer)
                )
            }
        },
        // karma coefficient, closed
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (fandom?.closed == true) {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        state = rememberTooltipState(isPersistent = false),
                        tooltip = {
                            PlainTooltip {
                                Text(stringResource(R.string.fandom_closed_tooltip))
                            }
                        },
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.fandom_closed_tooltip))
                    }
                }

                if (fandom?.karmaCof != null && fandom.karmaCof != 0L && fandom.karmaCof != 100L) {
                    Text(
                        text = "x${"%.2f".format(fandom.karmaCof / 100f)}",
                        color = if (fandom.karmaCof > 100) {
                            accentPositive
                        } else {
                            accentNegative
                        },
                        fontSize = 16.sp
                    )
                }
            }
        },
        modifier = Modifier
            .clickable {
                if (fandom == null) {
                    return@clickable
                }
                if (onClick != null) {
                    onClick(fandom)
                } else {
                    SFandom.instance(fandom, Navigator.TO)
                }
            }
    )
}

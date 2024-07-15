package com.sayzen.campfiresdk.compose.profile.badges.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.account.AccountBadge
import com.dzen.campfire.api.models.images.ImageRef
import com.sayzen.campfiresdk.compose.profile.badges.BadgeFlyout
import com.sayzen.campfiresdk.compose.util.shimmerExt
import com.sayzen.campfiresdk.fragment.BadgeListItem
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.java.tools.ToolsDate
import com.valentinilk.shimmer.Shimmer
import sh.sit.bonfire.images.RemoteImage
import sh.sit.bonfire.images.toRef

@Composable
fun BadgeListItem(item: BadgeListItem?, shimmer: Shimmer, onChoose: ((BadgeListItem?) -> Unit)?) {
    var isOpen by remember { mutableStateOf(false) }

    if (onChoose == null) {
        BadgeFlyout(
            open = isOpen,
            close = { isOpen = false },
            shortBadge = item?.let {
                AccountBadge().apply {
                    id = it.id.toLong()
                    miniImage = ImageRef(it.image.ui.i.toLong(), it.image.ui.u)
                }
            } ?: AccountBadge()
        )
    }

    ListItem(
        headlineContent = {
            Text(
                item?.name ?: "Still still loading",
                Modifier.shimmerExt(item == null, shimmer),
            )
        },
        overlineContent = {
            Text(
                item?.createdAt?.let { ToolsDate.dateToString(it.millis) } ?: "Loading",
                Modifier.shimmerExt(item == null, shimmer),
            )
        },
        supportingContent = {
            Text(
                item?.description ?: "Loading Loading Loading Loading Loading Loading Loading",
                Modifier.shimmerExt(item == null, shimmer),
            )
        },
        leadingContent = {
            if (item != null) {
                RemoteImage(
                    link = item.image.ui.toRef(),
                    contentDescription = item.name,
                    Modifier
                        .size(32.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(32.dp)
                        .shimmerExt(true, shimmer)
                )
            }
        },
        modifier = Modifier
            .clickable(item != null) {
                if (onChoose != null) {
                    Navigator.back()
                    onChoose(item!!)
                } else {
                    isOpen = true
                }
            }
    )
}

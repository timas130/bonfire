package com.sayzen.campfiresdk.compose.profile.badges.shelf

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.account.AccountBadge
import com.dzen.campfire.api.models.images.ImageRef
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.profile.badges.BadgeFlyout
import com.sayzen.campfiresdk.compose.util.shimmerExt
import com.sayzen.campfiresdk.fragment.BadgeShelfIcon
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.valentinilk.shimmer.Shimmer
import sh.sit.bonfire.auth.components.RemoteImage
import sh.sit.bonfire.auth.load

@Composable
fun RowScope.BadgeIcon(
    badgeIcon: BadgeShelfIcon?,
    model: BadgeShelfModel,
    index: Int,
    shimmer: Shimmer? = null,
) {
    val allowEditing by model.isEditingAllowed.collectAsState(initial = false)

    var flyoutOpen by remember { mutableStateOf(false) }

    if (badgeIcon != null) {
        BadgeFlyout(
            open = flyoutOpen,
            close = { flyoutOpen = false },
            shortBadge = AccountBadge().apply {
                id = badgeIcon.id.toLong()
                miniImage = ImageRef(badgeIcon.image.ui.i.toLong(), badgeIcon.image.ui.u)
            },
        )
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
    ) {
        FilledTonalIconButton(
            onClick = { flyoutOpen = true },
            enabled = badgeIcon != null,
            modifier = Modifier
                .shimmerExt(shimmer != null, shimmer)
                .fillMaxSize()
                .clip(CircleShape),
        ) {
            if (badgeIcon != null) {
                RemoteImage(
                    link = ImageLoader.load(badgeIcon.image.ui),
                    contentDescription = badgeIcon.name,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                )
            }
        }

        if (allowEditing) {
            FilledTonalIconButton(
                onClick = { model.onEditBadge(index) },
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.badge_edit),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

package com.sayzen.campfiresdk.compose.util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.whenStarted
import com.dzen.campfire.api.API
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.images.ImageRef
import com.sayzen.campfiresdk.compose.data.AccountDataSource
import com.sayzen.campfiresdk.compose.fandom.FandomDataSource
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.views.SplashAccountInfo
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.valentinilk.shimmer.Shimmer
import sh.sit.bonfire.images.RemoteImage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private fun Account.getLevelColor(): Color {
    ControllerOnline.set(id, lastOnlineDate)
    return when {
        !ControllerOnline.isOnline(id) -> Color(0xFF9E9E9E)
        ControllerApi.isBot(name) -> Color(0xFF000000)
        ControllerApi.isProtoadmin(this) -> Color(0xFFF97316)
        ControllerApi.isAdmin(this) && karma30 >= API.LVL_ADMINISTRATOR.karmaCount -> Color(0xFFD32F2F)
        ControllerApi.isModerator(this) && karma30 >= API.LVL_MODERATOR.karmaCount -> Color(0xFF1976D2)
        else -> Color(0xFF388E3C)
    }
}

private fun Account.getActiveImage(): ImageRef {
    val effectImage = ControllerEffects.getAvatar(this)
    if (effectImage != null) return effectImage

    val holidayImage = ControllerHoliday.getAvatar(this)
    if (holidayImage != null) return holidayImage

    if (image.isNotEmpty()) return image

    return ApiResources.AVATAR_1
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Avatar(
    account: Account,
    modifier: Modifier = Modifier,
    showLevel: Boolean = true,
    // this does not change behavior
    onLongClick: () -> Unit = {},
) {
    val hapticFeedback = LocalHapticFeedback.current

    val dataSource = remember(account) { AccountDataSource(account) }
    val updatedAccount by dataSource.flow.collectAsState()

    DisposableEffect(account) {
        onDispose {
            dataSource.destroy()
        }
    }

    Box(propagateMinConstraints = true) {
        RemoteImage(
            link = updatedAccount.getActiveImage(),
            contentDescription = updatedAccount.name,
            modifier = modifier
                .size(48.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(ControllerSettings.styleAvatarsRounding.dp))
                .combinedClickable(
                    onClick = {
                        SProfile.instance(account, Navigator.TO)
                    },
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                        SplashAccountInfo(account).asSheetShow()
                    }
                )
        )

        if (showLevel && updatedAccount.lvl >= 100) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(updatedAccount.getLevelColor())
                    .avatarSponsorCircles(updatedAccount.sponsorTimes.toInt())
            ) {
                Text(
                    text = if (ControllerApi.isBot(updatedAccount.name)) {
                        "B"
                    } else {
                        (updatedAccount.lvl / 100).toString()
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
fun Avatar(fandom: Fandom, modifier: Modifier = Modifier) {
    val dataSource = remember(fandom.id) { FandomDataSource(fandom) }

    val updatedFandom by dataSource.flow.collectAsState()

    RemoteImage(
        link = updatedFandom.image,
        contentDescription = updatedFandom.name,
        modifier = modifier
            .size(48.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(ControllerSettings.styleAvatarsRounding.dp))
            .clickable {
                SFandom.instance(fandom, Navigator.TO)
            }
    )
}

@Composable
fun AvatarShimmer(shimmer: Shimmer, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(48.dp)
            .clip(RoundedCornerShape(ControllerSettings.styleAvatarsRounding.dp))
            .shimmerExt(true, shimmer)
    )
}

private val sponsorColor = Color(0xFFFFEB3B)

private fun Modifier.avatarSponsorCircles(sponsorTimes: Int): Modifier = composed {
    var frameTime by remember { mutableLongStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.whenStarted {
            while (true) {
                frameTime = withFrameMillis { it }
            }
        }
    }

    drawWithContent {
        val currentTime = frameTime
        val loopTime = 5000f
        val circleRadius = 1.5.dp.toPx()

        val startAngle = (currentTime % loopTime) / loopTime * 2f * PI.toFloat()

        repeat(sponsorTimes) {
            val angle = startAngle + 2 * PI.toFloat() / sponsorTimes * it

            val x = cos(angle) * (size.width - 2 * circleRadius) / 2f + size.width / 2
            val y = sin(angle) * (size.height - 2 * circleRadius) / 2f + size.height / 2

            drawCircle(
                color = sponsorColor,
                radius = circleRadius,
                center = Offset(x, y),
            )
        }

        drawContent()
    }
}

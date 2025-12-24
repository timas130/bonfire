package com.sayzen.campfiresdk.compose.publication.post.pages.activity

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.models.publications.post.PageUserActivity
import com.posthog.PostHog
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.util.Avatar
import com.sayzen.campfiresdk.controllers.ControllerActivities
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.screens.activities.user_activities.relay_race.SRelayRaceInfo
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.screens.post.create.SplashTagsRelayRaceNextUser
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.java.tools.ToolsDate

internal val UserActivity.deadline: Long
    get() = tag_2 + API.ACTIVITIES_RELAY_RACE_TIME

internal val UserActivity.hasUser: Boolean
    get() = deadline > ControllerApi.currentTime()

@Composable
internal fun PageUserActivityRenderer(page: PageUserActivity) {
    val dataSource = remember(page.userActivity.id) { UserActivityDataSource(page.userActivity) }
    val userActivity by dataSource.flow.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(8.dp),
        onClick = {
            SRelayRaceInfo.instance(userActivity.id, Navigator.TO)
        },
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = userActivity.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Text(
                text = userActivity.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            UserActivityUser(userActivity)

            UserActivityButtons(userActivity)
        }
    }
}

@Composable
private fun UserActivityUser(userActivity: UserActivity) {
    AnimatedContent(
        targetState = Pair(userActivity.currentAccount, userActivity.hasUser),
        contentKey = { Pair(it.first.id, it.second) },
        label = "UserActivityUser",
    ) {
        if (it.second) { // if hasUser
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(
                    account = it.first,
                    modifier = Modifier.padding(end = 8.dp),
                )

                Text(
                    text = it.first.name,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                )

                UserActivityCountdown(stopAt = userActivity.deadline)
            }
        } else {
            Text(
                text = stringResource(R.string.activity_no_user),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun UserActivityCountdown(stopAt: Long) {
    val currentTime = remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    DisposableEffect(Unit) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                currentTime.longValue = System.currentTimeMillis()
                handler.postDelayed(this, 1000)
            }
        }

        handler.postDelayed(runnable, 1000)

        onDispose {
            handler.removeCallbacks(runnable)
        }
    }

    val remaining = stopAt - currentTime.longValue

    Text(
        text = ToolsDate.timeToString(remaining),
        style = MaterialTheme.typography.bodyMedium,
        softWrap = false,
    )
}

@Suppress("LocalVariableName")
@Composable
private fun UserActivityButtons(userActivity: UserActivity) {
    val _isCurrentUser = userActivity.hasUser && userActivity.currentAccount.id == ControllerApi.account.getId()
    val _isParticipant = userActivity.myMemberStatus == 1L
    val _canParticipate = userActivity.myPostId == 0L

    AnimatedContent(
        targetState = Triple(_isCurrentUser, _isParticipant, _canParticipate),
        label = "UserActivityButtons",
    ) {
        val isCurrentUser = it.first
        val isParticipant = it.second
        val canParticipate = it.third

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            if (isCurrentUser) {
                Button(onClick = {
                    toPostCreation(userActivity)
                }) {
                    Text(stringResource(R.string.activity_create_post))
                }

                TextButton(onClick = {
                    ControllerActivities.reject(userActivity.id)
                }) {
                    Text(stringResource(R.string.activity_reject))
                }
            } else if (isParticipant) {
                Button(onClick = {
                    ControllerActivities.no_member(userActivity.id)
                }) {
                    Text(stringResource(R.string.activity_leave))
                }
            } else if (canParticipate) {
                Button(onClick = {
                    ControllerActivities.member(userActivity.id)
                }) {
                    Text(stringResource(R.string.activity_join))
                }
            }
        }
    }
}

private fun toPostCreation(userActivity: UserActivity) {
    PostHog.capture("create_draft", properties = mapOf("from" to "activity_v2"))
    SplashTagsRelayRaceNextUser(userActivity.id) {
        SPostCreate.instance(
            fandomId = userActivity.fandom.id,
            languageId = userActivity.fandom.languageId,
            fandomName = userActivity.fandom.name,
            fandomImage = userActivity.fandom.image,
            postParams = SPostCreate.PostParams()
                .setActivity(userActivity)
                .setNextRelayRaceUserId(it),
            action = Navigator.TO
        )
    }.asSheetShow()
}

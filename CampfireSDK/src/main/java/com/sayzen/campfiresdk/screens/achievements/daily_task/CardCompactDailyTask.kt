package com.sayzen.campfiresdk.screens.achievements.daily_task

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.daily_tasks.DailyTaskInfo
import com.dzen.campfire.api.models.daily_tasks.DailyTaskType
import com.dzen.campfire.api.requests.achievements.RAchievementsInfo
import com.sayzen.campfiresdk.compose.ComposeCard
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.chat.EventChatNewBottomMessage
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.publications.EventCommentAdd
import com.sayzen.campfiresdk.models.events.publications.EventPostStatusChange
import com.sayzen.campfiresdk.models.events.publications.EventPublicationKarmaAdd
import com.sayzen.campfiresdk.screens.achievements.SAchievements
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.java.libs.eventBus.EventBus
import kotlinx.coroutines.flow.MutableStateFlow

class CardCompactDailyTask : ComposeCard() {
    init {
        EventBus
            .subscribe(EventPostStatusChange::class) {
                reloadIfTypeMatches(
                    DailyTaskType.PostInFandom,
                    DailyTaskType.CreatePostWithPageType,
                    DailyTaskType.CreatePosts
                )
            }
            .subscribe(EventCommentAdd::class) {
                reloadIfTypeMatches(
                    DailyTaskType.CommentInFandom,
                    DailyTaskType.PostComments,
                    DailyTaskType.CommentNewbiePost,
                    DailyTaskType.AnswerNewbieComment
                )
            }
            .subscribe(EventChatNewBottomMessage::class) {
                reloadIfTypeMatches(
                    DailyTaskType.AnswerInChat,
                    DailyTaskType.WriteMessages,
                )
            }
            .subscribe(EventPublicationKarmaAdd::class) {
                reloadIfTypeMatches(DailyTaskType.RatePublications)
            }
            .subscribe(EventNotification::class) {
                if (it.notification.getType() == API.NOTIF_KARMA_ADD) {
                    reloadIfTypeMatches(DailyTaskType.EarnAnyKarma, DailyTaskType.EarnPostKarma)
                }
            }
    }

    private var _task = MutableStateFlow<DailyTaskInfo?>(null)
    private var _isError = MutableStateFlow(false)

    @Composable
    override fun getBackground(): Color = Color.Transparent

    @Composable
    override fun Content() {
        LaunchedEffect(Unit) {
            load()
        }

        val task by _task.collectAsState()
        val isError by _isError.collectAsState()

        Surface(color = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface) {
            if (isError) {
                Text(
                    t(API_TRANSLATE.daily_task_combo_bonus),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                DailyTask(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable {
                            SAchievements.instance(action = Navigator.TO)
                        },
                    taskInfo = task,
                    compact = true,
                )
            }
        }
    }

    private fun load() {
        _isError.tryEmit(false)
        // omg is it possible? no update()???

        RAchievementsInfo(ControllerApi.account.getId())
            .onComplete {
                _task.tryEmit(it.dailyTask)
            }
            .onError {
                _isError.tryEmit(true)
            }
            .send(api)
    }

    private fun reloadIfTypeMatches(vararg types: DailyTaskType) {
        val taskType = _task.value?.task?.type ?: return
        for (type in types) {
            if (taskType == type) {
                load()
                break
            }
        }
    }

    fun createView(parent: ViewGroup): View {
        val view = instanceView(parent)
        bindCardView(view)
        return view
    }
}

package com.sayzen.campfiresdk.screens.achievements.daily_task

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.daily_tasks.DailyTaskInfo
import com.dzen.campfire.api.models.daily_tasks.DailyTaskType
import com.dzen.campfire.api.models.project.ProjectEvent
import com.dzen.campfire.api.requests.project.RProjectGetEvents
import com.sayzen.campfiresdk.compose.ComposeCard
import com.sayzen.campfiresdk.controllers.ControllerExternalLinks
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.java.tools.ToolsText

@Composable
private fun DailyTaskMultipliers(modifier: Modifier = Modifier, taskInfo: DailyTaskInfo) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(Modifier.width(8.dp))

        if (taskInfo.levelMultiplier > 0) {
            SuggestionChip(
                onClick = {
                    SplashAlert()
                        .setText(t(API_TRANSLATE.daily_task_newbie_explain))
                        .setOnEnter(t(API_TRANSLATE.app_ok))
                        .asSheetShow()
                },
                label = {
                    Text(
                        t(
                            API_TRANSLATE.daily_task_newbie_bonus,
                            "${(taskInfo.levelMultiplier * 100).toInt()}%"
                        )
                    )
                },
            )
        }

        SuggestionChip(
            onClick = {
                SplashAlert()
                    .setText(t(API_TRANSLATE.daily_task_combo_explain))
                    .setOnEnter(t(API_TRANSLATE.app_ok))
                    .asSheetShow()
            },
            label = {
                Text(
                    t(
                        API_TRANSLATE.daily_task_combo_bonus,
                        "${(taskInfo.comboMultiplier * 100 - 100).toInt()}%"
                    )
                )
            }
        )

        Spacer(Modifier.width(8.dp))
    }
}

@Composable
fun DailyTask(modifier: Modifier = Modifier, taskInfo: DailyTaskInfo?, compact: Boolean = false) {
    Column(modifier) {
        // task label
        val taskName = taskInfo?.let {
            when (it.task.type) {
                DailyTaskType.PostInFandom, DailyTaskType.CommentInFandom -> {
                    t(
                        API_TRANSLATE.dailyTaskNames[it.task.type] ?: API_TRANSLATE.daily_task_unknonwn,
                        it.fandomName ?: t(API_TRANSLATE.daily_task_deleted_fandom),
                    )
                }

                DailyTaskType.CommentNewbiePost, DailyTaskType.AnswerNewbieComment -> {
                    t(
                        API_TRANSLATE.dailyTaskNames[it.task.type] ?: API_TRANSLATE.daily_task_unknonwn,
                        ToolsText.numToStringRound(it.task.maxLevel / 100.0, 2),
                    )
                }

                DailyTaskType.CreatePostWithPageType -> {
                    t(
                        API_TRANSLATE.dailyTaskNames[it.task.type] ?: API_TRANSLATE.daily_task_unknonwn,
                        t(API_TRANSLATE.pageTypeNames[it.task.pageType] ?: API_TRANSLATE.post_page_unknown),
                    )
                }

                else -> {
                    t(API_TRANSLATE.dailyTaskNames[it.task.type] ?: API_TRANSLATE.daily_task_unknonwn)
                }
            }
        }
        Text(
            taskName ?: t(API_TRANSLATE.app_loading_dots),
            style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(bottom = if (compact) 4.dp else 8.dp),
        )

        // task progress indicator
        LinearProgressIndicator(
            progress = {
                taskInfo
                    ?.let { it.progress.toFloat() / it.task.amount.toFloat() }
                    ?.takeIf { it.isFinite() }
                    ?: 0f
            },
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp)),
        )

        // reward and progress information
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val textStyle = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium

            // keep this while taskInfo is still null, so it takes
            // up space and there's no layout shift
            // (unlike the old dt)
            Text(
                taskInfo?.let {
                    t(
                        API_TRANSLATE.daily_task_reward_lvl,
                        ToolsText.numToStringRound(it.possibleReward / 100.0, 2)
                    )
                } ?: " ",
                style = textStyle,
            )

            if (taskInfo != null) {
                val progress = taskInfo.progress.coerceAtMost(taskInfo.task.amount)
                if (taskInfo.task.type.karmaTask) {
                    Text(
                        t(API_TRANSLATE.daily_task_progress, progress / 100, taskInfo.task.amount / 100),
                        style = textStyle,
                    )
                } else {
                    Text(
                        t(API_TRANSLATE.daily_task_progress, progress, taskInfo.task.amount),
                        style = textStyle,
                    )
                }
            }
        }
    }
}

@Composable
private fun TasksTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(bottom = 8.dp, top = 16.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
    )
}

@Composable
private fun TasksDescription(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .alpha(0.7F)
    )
}

class PageDailyTasks(private val taskInfo: DailyTaskInfo) : ComposeCard() {
    @Composable
    override fun Content() {
        var events by remember { mutableStateOf<Array<ProjectEvent>?>(null) }

        LaunchedEffect(Unit) {
            RProjectGetEvents()
                .onComplete {
                    events = it.events
                }
                .onError {
                    ToolsToast.show(t(API_TRANSLATE.app_error))
                }
                .send(api)
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // daily task
            item {
                TasksTitle(t(API_TRANSLATE.daily_tasks))
                TasksDescription(t(API_TRANSLATE.daily_tasks_tutorial))
            }
            item {
                DailyTask(Modifier.padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp), taskInfo)

                // active multipliers
                DailyTaskMultipliers(taskInfo = taskInfo)
            }

            // events
            item {
                TasksTitle(t(API_TRANSLATE.events))
                TasksDescription(t(API_TRANSLATE.events_tutorial))
            }
            item {
                if (events.isNullOrEmpty()) {
                    Text(
                        if (events?.isEmpty() == true) {
                            t(API_TRANSLATE.events_empty)
                        } else if (events == null) {
                            t(API_TRANSLATE.app_loading_dots)
                        } else {
                            throw IllegalStateException()
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 0.dp)
                            .padding(bottom = 64.dp)
                    )
                }
            }
            items(events ?: arrayOf()) {
                Event(it)
            }
        }
    }
}

@Composable
private fun Event(event: ProjectEvent) {
    ListItem(
        headlineContent = { Text(event.title) },
        supportingContent = {
            Column {
                Text(event.description, modifier = Modifier.padding(bottom = 4.dp))
                LinearProgressIndicator(
                    progress = {
                        (event.progressCurrent.toFloat() / event.progressMax).coerceAtMost(1f)
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                )
            }
        },
        trailingContent = {
            if (event.url != null) {
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowRight,
                    "",
                )
            }
        },
        modifier = Modifier
            .clickable(event.url != null) {
                event.followLink()
            },
    )
}

fun ProjectEvent.followLink() {
    if (url!!.startsWith(API.DOMEN_DL)) {
        ControllerExternalLinks.openLink(url!!)
    } else {
        Navigator.to(EventWebpageScreen(this))
    }
}

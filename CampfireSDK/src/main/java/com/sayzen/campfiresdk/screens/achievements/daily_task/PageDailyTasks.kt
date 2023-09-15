package com.sayzen.campfiresdk.screens.achievements.daily_task

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.daily_tasks.DailyTaskInfo
import com.dzen.campfire.api.models.daily_tasks.DailyTaskType
import com.sayzen.campfiresdk.compose.ComposeCard
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.java.tools.ToolsText

@Composable
private fun DailyTaskBonuses(modifier: Modifier = Modifier, taskInfo: DailyTaskInfo) {
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
                    Text(t(
                        API_TRANSLATE.daily_task_newbie_bonus,
                        "${(taskInfo.levelMultiplier * 100).toInt()}%"
                    ))
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
                Text(t(
                    API_TRANSLATE.daily_task_combo_bonus,
                    "${(taskInfo.comboMultiplier * 100 - 100).toInt()}%"
                ))
            }
        )

        Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun DailyTask(modifier: Modifier = Modifier, taskInfo: DailyTaskInfo) {
    Column(modifier) {
        // task label
        val taskName = when (taskInfo.task.type) {
            DailyTaskType.PostInFandom, DailyTaskType.CommentInFandom -> {
                t(
                    API_TRANSLATE.dailyTaskNames[taskInfo.task.type] ?: API_TRANSLATE.daily_task_unknonwn,
                    taskInfo.fandomName ?: t(API_TRANSLATE.daily_task_deleted_fandom),
                )
            }
            DailyTaskType.CommentNewbiePost, DailyTaskType.AnswerNewbieComment -> {
                t(
                    API_TRANSLATE.dailyTaskNames[taskInfo.task.type] ?: API_TRANSLATE.daily_task_unknonwn,
                    ToolsText.numToStringRound(taskInfo.task.maxLevel / 100.0, 2),
                )
            }
            DailyTaskType.CreatePostWithPageType -> {
                t(
                    API_TRANSLATE.dailyTaskNames[taskInfo.task.type] ?: API_TRANSLATE.daily_task_unknonwn,
                    t(API_TRANSLATE.pageTypeNames[taskInfo.task.pageType] ?: API_TRANSLATE.post_page_unknown),
                )
            }
            else -> {
                t(API_TRANSLATE.dailyTaskNames[taskInfo.task.type] ?: API_TRANSLATE.daily_task_unknonwn)
            }
        }
        Text(
            taskName,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .padding(horizontal = 16.dp),
        )

        // task progress indicator
        LinearProgressIndicator(
            progress = taskInfo.progress.toFloat() / taskInfo.total.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(8.dp)),
        )

        // reward and progress information
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                t(
                    API_TRANSLATE.daily_task_reward_lvl,
                    ToolsText.numToStringRound(taskInfo.possibleReward / 100.0, 2)
                ),
                style = MaterialTheme.typography.bodyMedium,
            )

            if (taskInfo.task.type.karmaTask) {
                Text(
                    t(API_TRANSLATE.daily_task_progress, taskInfo.progress / 100, taskInfo.total / 100),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Text(
                    t(API_TRANSLATE.daily_task_progress, taskInfo.progress, taskInfo.total),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // active multipliers
        DailyTaskBonuses(taskInfo = taskInfo)
    }
}

@Composable
private fun TasksTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineMedium,
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
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .alpha(0.7F)
    )
}

class PageDailyTasks(private val taskInfo: DailyTaskInfo) : ComposeCard() {
    @Composable
    override fun Content() {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // daily task
            item {
                TasksTitle(t(API_TRANSLATE.daily_tasks))
                TasksDescription(t(API_TRANSLATE.daily_tasks_tutorial))
            }
            item {
                DailyTask(Modifier.padding(top = 16.dp), taskInfo)
            }

            // events
            item {
                TasksTitle(t(API_TRANSLATE.events))
                TasksDescription(t(API_TRANSLATE.events_tutorial))
            }
            item {
                Text(
                    t(API_TRANSLATE.events_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 64.dp)
                )
            }
        }
    }
}

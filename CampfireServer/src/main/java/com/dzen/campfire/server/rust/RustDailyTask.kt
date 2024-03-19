package com.dzen.campfire.server.rust

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.daily_tasks.DailyTask
import com.dzen.campfire.api.models.daily_tasks.DailyTaskInfo
import com.dzen.campfire.api.models.daily_tasks.DailyTaskType
import com.dzen.campfire.server.CheckInMutation
import com.dzen.campfire.server.DailyTaskFandomsQuery
import com.dzen.campfire.server.DailyTaskQuery
import com.dzen.campfire.server.fragment.DailyTaskFandom
import com.dzen.campfire.server.rust.ControllerRust.executeExt
import com.dzen.campfire.server.type.PageType

object RustDailyTask {
    fun getPossibleFandoms(userId: Long): List<DailyTaskFandom> {
        return ControllerRust.apollo.query(DailyTaskFandomsQuery(userId.toString()))
            .executeExt()
            .userById!!
            .dailyTaskFandoms
            .map { it.dailyTaskFandom }
    }

    fun getInfo(userId: Long): DailyTaskInfo {
        val resp = ControllerRust.apollo
            .query(DailyTaskQuery(userId.toString()))
            .executeExt()

        val dailyTaskInfo = resp.userById!!.dailyTask

        val task = dailyTaskInfo.task.let {
            val type = DailyTaskType.valueOf(it.__typename.removeSuffix("Task"))
            val pageType = when (it.onCreatePostWithPageTypeTask?.pageType) {
                PageType.TEXT -> API.PAGE_TYPE_TEXT
                PageType.IMAGE ->API.PAGE_TYPE_IMAGE
                PageType.IMAGES -> API.PAGE_TYPE_IMAGES
                PageType.LINK -> API.PAGE_TYPE_LINK
                PageType.QUOTE -> API.PAGE_TYPE_QUOTE
                PageType.SPOILER -> API.PAGE_TYPE_SPOILER
                PageType.POLLING -> API.PAGE_TYPE_POLLING
                PageType.VIDEO -> API.PAGE_TYPE_VIDEO
                PageType.TABLE -> API.PAGE_TYPE_TABLE
                PageType.DOWNLOAD -> API.PAGE_TYPE_DOWNLOAD
                PageType.CAMPFIRE_OBJECT -> API.PAGE_TYPE_CAMPFIRE_OBJECT
                PageType.USER_ACTIVITY -> API.PAGE_TYPE_USER_ACTIVITY
                PageType.LINK_IMAGE -> API.PAGE_TYPE_LINK_IMAGE
                PageType.CODE -> API.PAGE_TYPE_CODE
                else -> null
            }
            val amount = it.amount
            val fandomId = it.onPostInFandomTask?.fandomId ?: it.onCommentInFandomTask?.fandomId
            val maxLevel = it.onAnswerNewbieCommentTask?.maxLevel ?: it.onCommentNewbiePostTask?.maxLevel

            DailyTask().apply {
                this.type = type
                this.pageType = pageType ?: 0L
                this.amount = amount.toLong()
                this.fandomId = (fandomId ?: 0).toLong()
                this.maxLevel = (maxLevel ?: 0).toLong()
            }
        }

        return DailyTaskInfo().apply {
            this.task = task
            this.total = task.amount
            this.progress = dailyTaskInfo.progress.toLong()
            this.totalLevels = dailyTaskInfo.totalLevels.toLong()
            this.levelMultiplier = dailyTaskInfo.levelMultiplier.toFloat()
            this.comboMultiplier = dailyTaskInfo.comboMultiplier.toFloat()
            this.possibleReward = dailyTaskInfo.possibleReward.toLong()
            this.fandomName = dailyTaskInfo.fandomName
        }
    }

    fun checkIn(userId: Long) {
        ControllerRust.apollo.mutation(CheckInMutation(userId.toString()))
            .executeExt()
    }
}

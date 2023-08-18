package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.QuestInfo
import com.dzen.campfire.api.models.notifications.project.NotificationQuestFinish
import com.dzen.campfire.api.models.notifications.project.NotificationQuestProgress
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserQuestFinish
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiAccount
import com.sup.dev.java.tools.ToolsDate
import java.util.ArrayList

object ControllerQuests {

    private val cash = HashMap<String, Long>()
    private var lastCashUpdate = 0L

    fun updateCash(){
        val start = ToolsDate.getStartOfDay()
        if(start != lastCashUpdate){
            lastCashUpdate = start
            cash.clear()
        }
    }

    fun isAlreadyFinishedToday(accountId: Long): Boolean {
        return ControllerOptimizer.getCollisionDaily(accountId, API.COLLISION_ACCOUNT_QUEST) != null
    }

    fun addQuestWithCheck(apiAccount: ApiAccount, quest: QuestInfo) {
        addQuestWithCheck(apiAccount.id, apiAccount.accessTag, quest)
    }

    fun addQuestWithCheck(accountId: Long, accountLvl: Long, quest: QuestInfo) {
        if (getQuestIndex(accountId) != quest.index) return
        ControllerSubThread.inSub("ControllerQuests.addQuestWithCheck(${quest.index})") {
            addQuestWithCheckNow(accountId, accountLvl, quest)
        }
    }

    fun addQuestWithCheckNow(accountId: Long, accountLvl: Long, quest: QuestInfo) {
        val questIndex = getQuestIndex(accountId)
        if (isAlreadyFinishedToday(accountId)) return
        if (questIndex != quest.index) return

        val value = getValue(accountId, questIndex)
        ControllerNotifications.push(accountId, NotificationQuestProgress(questIndex, value))
        if (!quest.questIsFinished(accountLvl, value)) return

        ControllerOptimizer.putCollisionDateDaily(accountId, API.COLLISION_ACCOUNT_QUEST, 0, System.currentTimeMillis())

        ControllerAchievements.addAchievementWithCheck(accountId, API.ACHI_QUESTS)

        ControllerNotifications.push(accountId, NotificationQuestFinish(questIndex, value))
        val v = ControllerAccounts.get(accountId, TAccounts.name, TAccounts.img_id, TAccounts.sex)
        ControllerPublications.event(ApiEventUserQuestFinish(accountId, v.next(), v.next(), v.next(), questIndex, value), accountId)
    }

    fun addQuestProgress(apiAccount: ApiAccount, quest: QuestInfo, value:Long){
        addQuestProgress(apiAccount.id, apiAccount.accessTag, quest, value)
    }

    fun addQuestProgress(accountId: Long, accountLvl: Long, quest: QuestInfo, value:Long){
        setQuestProgress(accountId, accountLvl, quest, getValue(accountId, quest.index) + value)
    }

    fun setQuestProgressIfLess(accountId: Long, accountLvl: Long, quest: QuestInfo, value:Long){
        if(getValue(accountId, quest.index) > value) return
        setQuestProgress(accountId, accountLvl, quest, value)
    }

    fun setQuestProgress(accountId: Long, accountLvl: Long, quest: QuestInfo, value:Long){
        cash["" + accountId +"_"+quest.index] = value
        addQuestWithCheck(accountId, accountLvl, quest)
    }

    fun getValue(apiAccount: ApiAccount): Long {
        return getValue(apiAccount.id, getQuestIndex(apiAccount))
    }

    fun getValue(accountId: Long, questIndex: Long): Long {
        updateCash()
        return cash["" + accountId +"_"+questIndex] ?:0
    }

    //
    //  Quests index
    //

    private val QUESTS = ArrayList<QuestInfo>()

    fun getQuestIndex(account: ApiAccount): Long {
        return getQuestIndex(account.id)
    }

    fun getQuestIndex(accountId: Long): Long {

        if (QUESTS.isEmpty()) {
            QUESTS.add(API.QUEST_POSTS)
            QUESTS.add(API.QUEST_POST_KARMA)
            QUESTS.add(API.QUEST_CHAT)
            QUESTS.add(API.QUEST_COMMENTS)
            QUESTS.add(API.QUEST_COMMENTS_KARMA)
            QUESTS.add(API.QUEST_RATES)
            QUESTS.add(API.QUEST_KARMA)
            QUESTS.add(API.QUEST_ACTIVITIES)
        }

        val x = ((ToolsDate.getStartOfDay() + 1000L * 60 * 60 * 24) / (1000L * 60 * 60 * 24)) % QUESTS.size
        return QUESTS.get(((accountId % QUESTS.size) + x).toInt() % QUESTS.size).index
    }

}

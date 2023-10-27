package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.AchievementInfo
import com.dzen.campfire.api.models.notifications.account.NotificationAchievement
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAchievement
import com.dzen.campfire.server.rust.RustAchievements
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentSkipListSet

object ControllerAchievements {
    fun recount(accountId: Long): RustAchievements.LevelRecountReport {
        val previousReportS = ControllerCollisions.getCollisionValue2(accountId, API.COLLISION_ACCOUNT_ACHIEVEMENTS)
        val previousReport = if (previousReportS.isNotEmpty()) {
            try {
                Json.decodeFromString<RustAchievements.LevelRecountReport>(previousReportS)
            } catch (e: SerializationException) {
                null
            }
        } else {
            null
        }

        val report = RustAchievements.getForUser(accountId)
        for ((_, achievement) in report.achievements) {
            val previousLvl = ((previousReport?.achievements?.get(achievement.id)?.target ?: 9999999) + 1).toInt()
            val achievementTarget = ((achievement.target ?: -1) + 1).toInt()

            if (previousLvl < achievementTarget) {
                // on new achievement level
                ControllerNotifications.push(accountId, NotificationAchievement(
                    achiIndex = achievement.id,
                    achiLvl = achievementTarget,
                ))
                val account = ControllerAccounts.get(accountId, TAccounts.name, TAccounts.img_id, TAccounts.sex)
                ControllerPublications.event(
                    ApiEventUserAchievement(
                        ownerAccountId = accountId,
                        ownerAccountName = account.next(),
                        ownerAccountImageId = account.next(),
                        ownerAccountSex = account.next(),
                        achievementIndex = achievement.id,
                        achievementLvl = achievementTarget.toLong()
                    ),
                    accountId
                )
            }
        }

        ControllerCollisions.updateOrCreateValue2(
            ownerId = accountId,
            collisionType = API.COLLISION_ACCOUNT_ACHIEVEMENTS,
            value2 = Json.encodeToString(report),
        )

        Database.update("ControllerAchievements.recount setLvl", SqlQueryUpdate(TAccounts.NAME)
                .where(TAccounts.id, "=", accountId)
                .update(TAccounts.lvl, report.totalLevel))

        return report
    }

    private val activeRecounts = ConcurrentSkipListSet<Long>()

    fun addAchievementWithCheck(accountId: Long, achievementInfo: AchievementInfo) {
        if (accountId < 1) return
        ControllerSubThread.inSub("ControllerAchievements.addAchievementWithCheck(${achievementInfo.index})") {
            if (!activeRecounts.add(accountId)) return@inSub
            recount(accountId)
            activeRecounts.remove(accountId)
        }
    }
}

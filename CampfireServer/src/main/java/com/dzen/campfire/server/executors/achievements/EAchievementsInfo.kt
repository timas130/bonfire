package com.dzen.campfire.server.executors.achievements

import com.dzen.campfire.api.requests.achievements.RAchievementsInfo
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerAchievements
import com.dzen.campfire.server.tables.TAccounts

class EAchievementsInfo : RAchievementsInfo(0) {
    private val indexes = mutableListOf<Long>()
    private val lvls = mutableListOf<Long>()

    override fun execute(): Response {
        val v = ControllerAccounts.get(accountId, TAccounts.karma_count_30)

        val karma30:Long = v.next()

        val report = ControllerAchievements.recount(accountId)
        for ((_, achievement) in report.achievements) {
            indexes.add(achievement.id)
            lvls.add((achievement.target ?: -1) + 1)
        }

        return Response(karma30, report.totalLevel, indexes.toTypedArray(), lvls.toTypedArray())
    }

}

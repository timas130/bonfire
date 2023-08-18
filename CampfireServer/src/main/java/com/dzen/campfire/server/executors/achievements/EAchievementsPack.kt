package com.dzen.campfire.server.executors.achievements


import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.AchievementInfo
import com.dzen.campfire.api.requests.achievements.RAchievementsInfo
import com.dzen.campfire.api.requests.achievements.RAchievementsPack
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerAchievements
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java.libs.debug.Debug


class EAchievementsPack : RAchievementsPack(0, 0) {

    val indexes = ArrayList<Long>()
    val progress = ArrayList<Long>()

    override fun execute(): Response {

        if (packIndex == 1 || packIndex == 0) for (i in API.ACHI_PACK_1) load(i)
        if (packIndex == 2 || packIndex == 0) for (i in API.ACHI_PACK_2) load(i)
        if (packIndex == 3 || packIndex == 0) for (i in API.ACHI_PACK_3) load(i)
        if (packIndex == 4 || packIndex == 0) for (i in API.ACHI_PACK_4) load(i)
        if (packIndex == 5 || packIndex == 0) for (i in API.ACHI_PACK_5) load(i)
        if (packIndex == 6 || packIndex == 0) for (i in API.ACHI_PACK_6) load(i)

        return Response(indexes.toTypedArray(), progress.toTypedArray())
    }

    private fun load(a: AchievementInfo) {
        indexes.add(a.index)
        val v = ControllerAchievements.getValue(accountId, a)
        progress.add(v)
    }

}

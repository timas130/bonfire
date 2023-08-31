package com.dzen.campfire.server.executors.achievements

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.AchievementInfo
import com.dzen.campfire.api.requests.achievements.RAchievementsPack
import com.dzen.campfire.server.controllers.ControllerAchievements
import com.dzen.campfire.server.rust.RustAchievements

class EAchievementsPack : RAchievementsPack(0, 0) {
    private lateinit var report: RustAchievements.LevelRecountReport

    private val indexes = mutableListOf<Long>()
    private val progress = mutableListOf<Long>()

    override fun execute(): Response {
        report = ControllerAchievements.recount(accountId)

        if (packIndex == 1 || packIndex == 0) for (i in API.ACHI_PACK_1) load(i)
        if (packIndex == 2 || packIndex == 0) for (i in API.ACHI_PACK_2) load(i)
        if (packIndex == 3 || packIndex == 0) for (i in API.ACHI_PACK_3) load(i)
        if (packIndex == 4 || packIndex == 0) for (i in API.ACHI_PACK_4) load(i)
        if (packIndex == 5 || packIndex == 0) for (i in API.ACHI_PACK_5) load(i)
        if (packIndex == 6 || packIndex == 0) for (i in API.ACHI_PACK_6) load(i)

        return Response(indexes.toTypedArray(), progress.toTypedArray())
    }

    private fun load(a: AchievementInfo) {
        if (report.achievements.containsKey(a.index)) {
            indexes.add(a.index)
            progress.add(report.achievements[a.index]!!.count)
        }
    }
}

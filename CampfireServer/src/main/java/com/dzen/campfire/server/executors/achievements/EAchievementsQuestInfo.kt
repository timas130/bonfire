package com.dzen.campfire.server.executors.achievements

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.achievements.RAchievementsQuestInfo
import com.dzen.campfire.api.tools.ApiException

class EAchievementsQuestInfo : RAchievementsQuestInfo() {
    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {
        return Response(API.QUEST_UNKNOWN.index, -1, false)
    }
}

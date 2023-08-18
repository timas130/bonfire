package com.dzen.campfire.server.executors.achievements

import com.dzen.campfire.api.requests.achievements.RAchievementsQuestInfo
import com.dzen.campfire.server.controllers.ControllerQuests
import com.dzen.campfire.api.tools.ApiException

class EAchievementsQuestInfo : RAchievementsQuestInfo() {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {

        val questFinished: Boolean = ControllerQuests.isAlreadyFinishedToday(apiAccount.id)
        val questIndex:Long = ControllerQuests.getQuestIndex(apiAccount)
        val questProgress = if (questFinished) 0 else ControllerQuests.getValue(apiAccount)

        return Response(questIndex, questProgress, questFinished)
    }

}

package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.requests.project.RProjectMiniGameAddScore
import com.dzen.campfire.server.controllers.ControllerOptimizer

class EProjectMiniGameAddScore : RProjectMiniGameAddScore(0, 0) {

    override fun check() {
        if (miniGameScoreHumansAdd < 0) miniGameScoreHumansAdd = 0
        if (miniGameScoreRobotsAdd < 0) miniGameScoreRobotsAdd = 0
    }

    override fun execute(): Response {

        if (miniGameScoreHumansAdd > 0) ControllerOptimizer.addMiniGameHumans(miniGameScoreHumansAdd)
        if (miniGameScoreRobotsAdd > 0) ControllerOptimizer.addMiniGameRobots(miniGameScoreRobotsAdd)

        return Response()
    }


}

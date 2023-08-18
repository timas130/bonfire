package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.requests.project.RProjectMiniGameGetinfo
import com.dzen.campfire.server.controllers.ControllerOptimizer

class EProjectMiniGameGetinfo : RProjectMiniGameGetinfo() {

    override fun check() {
    }

    override fun execute(): Response {

        return Response(ControllerOptimizer.getMiniGameHumans(), ControllerOptimizer.getMiniGameRobots())
    }


}

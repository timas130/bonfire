package com.dzen.campfire.server.executors.achievements

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.achievements.RAchievementsOnFinish
import com.dzen.campfire.server.controllers.ControllerAchievements
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.controllers.ControllerOptimizer

class EAchievementsOnFinish : RAchievementsOnFinish(0) {

    override fun execute(): Response {
        when (achievementIndex) {
            API.ACHI_APP_SHARE.index -> {
                ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_SHARE_APP)
                ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_APP_SHARE)
            }
            API.ACHI_RULES_MODERATOR.index -> {
                ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_RULES_MODER)
                ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_RULES_MODERATOR)
            }
            API.ACHI_RULES_USER.index -> {
                ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_RULES_USER)
                ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_RULES_USER)
            }
            API.ACHI_FIREWORKS.index -> {
                ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_FAERWORKS)
                ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_FIREWORKS)
            }
        }
        return Response()
    }
}

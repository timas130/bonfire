package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListAdd
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListAdd
import com.dzen.campfire.server.controllers.ControllerAchievements
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.controllers.ControllerViceroy
import com.dzen.campfire.server.optimizers.OptimizerBlackAccountsCount
import com.dzen.campfire.server.optimizers.OptimizerBlackFandomsCount

class EFandomsBlackListAdd : RFandomsBlackListAdd(0) {

    override fun check() {

    }

    override fun execute(): Response {

        ControllerCollisions.putCollisionWithCheck(apiAccount.id, fandomId, API.COLLISION_ACCOUNT_BLACK_LIST_FANDOM)
        OptimizerBlackFandomsCount.increment(apiAccount.id)

        return Response()
    }


}

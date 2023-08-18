package com.dzen.campfire.server.executors.accounts


import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsStatusSet
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.api.tools.ApiException

class EAccountsStatusSet : RAccountsStatusSet("") {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_CAN_CHANGE_STATUS)
        if(status.length > API.ACCOUNT_STATUS_MAX_L) throw ApiException(E_BAD_SIZE)
    }

    override fun execute(): Response {
        ControllerCollisions.removeCollisions(apiAccount.id, API.COLLISION_ACCOUNT_STATUS)
        ControllerCollisions.putCollisionValue2(apiAccount.id, API.COLLISION_ACCOUNT_STATUS, status)
        return Response()
    }


}
package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsBioSetAge
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.api.tools.ApiException

class EAccountsBioSetAge : RAccountsBioSetAge(0) {

    override fun check() {
        if(age < 0 || age > API.ACCOUNT_AGE_MAX) throw ApiException(E_BAD_AGE)
        ControllerAccounts.checkAccountBanned(apiAccount.id)
    }

    override fun execute(): Response {

        ControllerCollisions.updateOrCreateValue1(apiAccount.id,  API.COLLISION_ACCOUNT_AGE, age)

        return Response()
    }


}
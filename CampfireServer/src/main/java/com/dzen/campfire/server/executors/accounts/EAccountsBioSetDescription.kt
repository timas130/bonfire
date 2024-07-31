package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsBioSetDescription
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerCensor
import com.dzen.campfire.server.controllers.ControllerCollisions

class EAccountsBioSetDescription : RAccountsBioSetDescription("") {
    override fun check() {
        description = ControllerCensor.cens(description)
        if (description.length > API.ACCOUNT_DESCRIPTION_MAX_L) throw ApiException(E_BAD_SIZE)
        ControllerAccounts.checkAccountBanned(apiAccount.id)
    }

    override fun execute(): Response {
        ControllerCollisions.updateOrCreateValue2(apiAccount.id, API.COLLISION_ACCOUNT_DESCRIPTION, description)

        return Response(description)
    }
}

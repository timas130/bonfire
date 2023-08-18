package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsProtoadminAutorization
import com.dzen.campfire.server.app.AccountProviderImpl
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.api.tools.ApiException

class EAccountsProtoadminAutorization : RAccountsProtoadminAutorization(0) {

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_PROTOADMIN)
    }

    override fun execute(): Response {

        AccountProviderImpl.PROTOADMIN_AUTORIZATION_ID = accountId

        return Response()
    }


}
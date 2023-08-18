package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsGet
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts

class EAccountsGet : RAccountsGet(0, "") {

    override fun check() {

    }

    override fun execute(): Response {
        if(accountId == 0L){
            val account = (ControllerAccounts.getAccount(ControllerAccounts.getByName(accountName))) ?: throw ApiException(API.ERROR_GONE)
            return Response(account)
        }
        val account = ControllerAccounts.getAccount(accountId) ?: throw ApiException(API.ERROR_GONE)
        return Response(account)
    }


}

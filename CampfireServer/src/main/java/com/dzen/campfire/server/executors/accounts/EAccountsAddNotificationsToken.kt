package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsAddNotificationsToken
import com.dzen.campfire.server.controllers.ControllerAccounts

class EAccountsAddNotificationsToken : RAccountsAddNotificationsToken("") {


    override fun check() {
    }

    override fun execute(): Response {
        ControllerAccounts.addNotificationToken(apiAccount.id, token)
        return Response()
    }

}
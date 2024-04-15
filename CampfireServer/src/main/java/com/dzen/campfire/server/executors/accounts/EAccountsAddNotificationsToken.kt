package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsAddNotificationsToken
import com.dzen.campfire.server.rust.RustNotifications
import com.dzen.campfire.server.type.NotificationTokenType

class EAccountsAddNotificationsToken : RAccountsAddNotificationsToken("") {


    override fun check() {
    }

    override fun execute(): Response {
        RustNotifications.setToken(accessToken!!, NotificationTokenType.FCM, token)
        return Response()
    }

}

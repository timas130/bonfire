package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsNotificationsView
import com.dzen.campfire.server.rust.RustNotifications

class EAccountsNotificationsView : RAccountsNotificationsView(emptyArray(), emptyArray()) {
    override fun check() {

    }

    override fun execute(): Response {
        if (notificationIds.isNotEmpty()) {
            for (id in notificationIds) {
                RustNotifications.read(accessToken!!, id)
            }
        } else {
            // notificationTypes is always empty anyway xd
            RustNotifications.readAll(accessToken!!)
        }

        return Response()
    }
}

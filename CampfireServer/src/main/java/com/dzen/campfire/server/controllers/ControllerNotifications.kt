package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.models.notifications.Notification
import com.dzen.campfire.server.rust.RustNotifications

object ControllerNotifications {
    fun push(accountId: Long, notification: Notification) {
        RustNotifications.post(accountId, notification)
    }

    fun push(accountIds: Collection<Long>, notification: Notification) {
        RustNotifications.post(accountIds, notification)
    }

    fun push(accountIds: Array<Long>, notification: Notification) {
        RustNotifications.post(accountIds, notification)
    }
}

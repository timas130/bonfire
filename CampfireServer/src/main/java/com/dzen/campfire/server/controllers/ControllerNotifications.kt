package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.models.notifications.Notification
import com.dzen.campfire.server.rust.RustNotifications

object ControllerNotifications {
    private fun hydrateImages(notification: Notification): Notification {
        notification.fillImageRefs(ControllerResources)
        return notification
    }

    fun push(accountId: Long, notification: Notification) {
        RustNotifications.post(accountId, hydrateImages(notification))
    }

    fun push(accountIds: Collection<Long>, notification: Notification) {
        RustNotifications.post(accountIds, hydrateImages(notification))
    }

    fun push(accountIds: Array<Long>, notification: Notification) {
        RustNotifications.post(accountIds, hydrateImages(notification))
    }
}

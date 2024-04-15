package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.Notification
import com.dzen.campfire.api.requests.accounts.RAccountsNotificationsGetAll
import com.dzen.campfire.server.rust.RustNotifications
import com.sup.dev.java.libs.json.Json

class EAccountsNotificationsGetAll : RAccountsNotificationsGetAll(0, emptyArray(), false) {
    override fun check() {

    }

    override fun execute(): Response {
        // future me:
        // 1. `otherEnabled` makes the `filters` array a blocklist instead of an allowlist
        // 2. if `filters` is empty, no filter is applied (even if otherEnabled is true)

        val typeFilter = if (!otherEnabled) {
            filters.toList().takeIf { it.isNotEmpty() } ?: API.notificationTypes.toList()
        } else {
            val filterTypes = API.notificationTypes.toMutableSet()
            for (filter in filters) {
                filterTypes.remove(filter)
            }
            filterTypes.toList()
        }.map { it.toInt() }

        val notifications = RustNotifications.getAllNotifications(accessToken!!, offsetDate, typeFilter)
            .filter { it.payload.onLegacyNotification != null }
            .map {
                Notification.instance(Json(it.payload.onLegacyNotification!!.content)).apply {
                    this.id = it.id.toLong()
                    this.dateCreate = it.createdAt.millis
                    this.status = if (it.read) 1 else 0
                }
            }
            .toTypedArray()

        return Response(notifications)
    }
}

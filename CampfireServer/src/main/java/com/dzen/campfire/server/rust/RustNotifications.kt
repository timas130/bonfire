package com.dzen.campfire.server.rust

import com.apollographql.apollo3.api.Optional
import com.dzen.campfire.api.models.notifications.Notification
import com.dzen.campfire.server.*
import com.dzen.campfire.server.rust.ControllerRust.executeExt
import com.dzen.campfire.server.type.*
import com.sup.dev.java.libs.json.Json
import kotlin.random.Random

object RustNotifications {
    fun post(userId: Long, notification: Notification) {
        post(listOf(userId), notification)
    }

    fun post(userIds: Array<Long>, notification: Notification) {
        post(userIds.toList(), notification)
    }

    fun post(userIds: Collection<Long>, notification: Notification) {
        notification.randomCode = Random.nextInt().toLong()
        post(NotificationInput(
            recipients = userIds
                .map { NotificationRecipient(user = Optional.present(UserRecipient(it.toInt()))) },
            payload = NotificationPayloadInput(
                legacy = Optional.present(
                    LegacyNotificationInput(notification.json(true, Json()).toString())
                ),
            ),
            ephemeral = notification.isShadow(),
            onlineOnly = !notification.isNeedForcePush(),
        ))
    }

    fun post(input: NotificationInput) {
        ControllerRust.apollo
            .mutation(PostNotificationMutation(input))
            .executeExt()
    }

    fun getAllNotifications(accessToken: String, offsetDate: Long, typeFilter: List<Int>? = null): List<GetAllNotificationsQuery.Notification> {
        val before = offsetDate.takeUnless { it == 0L }?.let { DateTime(it) }
        return ControllerRust.apollo
            .query(GetAllNotificationsQuery(
                before = Optional.presentIfNotNull(before),
                typeFilter = Optional.presentIfNotNull(typeFilter),
            ))
            .addHttpHeader("Authorization", "Bearer $accessToken")
            .executeExt()
            .notifications
    }

    fun read(accessToken: String, notificationId: Long) {
        ControllerRust.apollo
            .mutation(ReadNotificationMutation(notificationId = notificationId.toString()))
            .addHttpHeader("Authorization", "Bearer $accessToken")
            .executeExt()
    }

    fun readAll(accessToken: String) {
        ControllerRust.apollo
            .mutation(ReadAllNotificationsMutation())
            .addHttpHeader("Authorization", "Bearer $accessToken")
            .executeExt()
    }

    fun setToken(accessToken: String, tokenType: NotificationTokenType, token: String) {
        ControllerRust.apollo
            .mutation(SetNotificationTokenMutation(tokenType, token))
            .addHttpHeader("Authorization", "Bearer $accessToken")
            .executeExt()
    }
}

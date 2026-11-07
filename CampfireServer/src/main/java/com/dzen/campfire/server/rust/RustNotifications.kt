package com.dzen.campfire.server.rust

import com.apollographql.apollo3.api.Optional
import com.dzen.campfire.api.models.notifications.Notification
import com.dzen.campfire.server.*
import com.dzen.campfire.server.rust.ControllerRust.executeExt
import com.sup.dev.java.libs.json.Json
import sh.sit.bonfire.schema.DateTime
import sh.sit.schema.type.*
import kotlin.random.Random

object RustNotifications {
    private const val PUSH_PAYLOAD_MAX_BYTES = 3900

    internal fun post(userId: Long, notification: Notification) {
        post(listOf(userId), notification)
    }

    internal fun post(userIds: Array<Long>, notification: Notification) {
        post(userIds.toList(), notification)
    }

    internal fun post(userIds: Collection<Long>, notification: Notification) {
        notification.randomCode = Random.nextInt().toLong()
        post(NotificationInput(
            recipients = userIds
                .map { NotificationRecipient(user = Optional.present(UserRecipient(it.toInt()))) },
            payload = NotificationPayloadInput(
                legacy = Optional.present(LegacyNotificationInput(pushPayload(notification))),
            ),
            ephemeral = notification.isShadow(),
            onlineOnly = !notification.isNeedForcePush(),
        ))
    }

    private fun pushPayload(notification: Notification): String {
        val payload = notification.json(true, Json()).toString()
        if (payload.toByteArray().size <= PUSH_PAYLOAD_MAX_BYTES) return payload

        val copy = Notification.instance(Json(payload))
        if (!copy.shortenForPush()) return payload
        return copy.json(true, Json()).toString()
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

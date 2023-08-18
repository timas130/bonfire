package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.models.notifications.chat.NotificationChatTyping
import com.dzen.campfire.api.models.publications.EventPublicationInstance
import com.sayzen.campfiresdk.models.events.account.EventAccountOnlineChanged
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerOnline {


    var online = HashMap<Long, Item2<Long, Boolean>>()

    private val eventBus = EventBus
            .subscribe(EventPublicationInstance::class) { this.set(it.publication.creator.id, it.publication.creator.lastOnlineDate) }
            .subscribe(EventNotification::class) {
                if (it.notification is NotificationChatTyping) set(it.notification.accountId, System.currentTimeMillis())
            }

    fun isOnline(accountId: Long) = isOnlineCheck(get(accountId))

    fun get(accountId: Long): Long {

        if(accountId == ControllerApi.account.getId()) return System.currentTimeMillis()

        val item = online[accountId] ?: return 0

        val lastOnlineTile = item.a1
        val lastState = item.a2
        val isOnline = isOnlineCheck(lastOnlineTile)

        if (lastState != isOnline) {
            item.a2 = isOnline
            EventBus.post(EventAccountOnlineChanged(accountId, lastOnlineTile))
        }

        return lastOnlineTile
    }

    private fun isOnlineCheck(time: Long) = time > ControllerApi.currentTime() - 1000L * 60L * 15L

    fun set(accountId: Long, time: Long) {
        val item = online[accountId]

        if (item != null) {
            if (item.a1 >= time) return
            item.a1 = time
        } else {
            online[accountId] = Item2(time, isOnlineCheck(time))
        }

        EventBus.post(EventAccountOnlineChanged(accountId, time))
    }



}
package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.models.notifications.project.NotificationProjectABParamsChanged
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json

object ControllerABParams {

    private var ABParams: Json = Json()

    private val eventBus = EventBus.subscribe(EventNotification::class) {
        if (it.notification is NotificationProjectABParamsChanged) set((it).notification.ABParams)
    }

    fun set(ABParams: Json) {
        this.ABParams = ABParams
    }

}
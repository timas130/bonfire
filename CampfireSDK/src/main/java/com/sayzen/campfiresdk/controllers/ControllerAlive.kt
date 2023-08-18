package com.sayzen.campfiresdk.controllers

import android.content.Intent
import com.dzen.campfire.api.models.notifications.project.NotificationAlive
import com.dzen.campfire.api.requests.project.RProjectVersionGet
import com.dzen.campfire.api_media.requests.RResourcesGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.account.EventAccountCurrentChanged
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.android.tools.ToolsJobScheduler
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsThreads

object ControllerAlive {

    private val eventBus = EventBus
            .subscribe(EventAccountCurrentChanged::class) { schedule() }
            .subscribe(EventNotification::class) { if (it.notification is NotificationAlive) onPush() }

    fun init() {
    }

    private fun onPush() {
        info("ControllerAlive", "push")
        ToolsStorage.put("ControllerAlive.push", System.currentTimeMillis())

    }

    private fun schedule() {
        if (ControllerApi.account.getId() != 1L) return
        info("ControllerAlive", "subscribe")
        ToolsJobScheduler.scheduleJob(15, 1000L * 60 * 5) {
            info("ControllerAlive", "start check")
            checkServer()
            checkServerMedia()
            checkPush()
            schedule()
        }
    }

    private fun checkServer(tryCount: Int = 10) {
        RProjectVersionGet()
                .onComplete {
                    info("ControllerAlive", "Check server done")
                }
                .onError {
                    info("ControllerAlive", "Check server ERROR")
                    if (tryCount <= 0)
                        ControllerNotifications.chanelOther.post(R.drawable.logo_campfire_alpha_black_and_white_no_margins, "Алерт!", "Сервер недоступен!", Intent(), "ControllerAlive_1")
                    else
                        ToolsThreads.main(10000) { checkServerMedia(tryCount - 1) }
                }
                .send(api)
    }

    private fun checkServerMedia(tryCount: Int = 10) {
        RResourcesGet(1)
                .onComplete {
                    info("ControllerAlive", "Check media server done")
                }
                .onError {
                    info("ControllerAlive", "Check media server ERROR")
                    if (tryCount <= 0)
                        ControllerNotifications.chanelOther.post(R.drawable.logo_campfire_alpha_black_and_white_no_margins, "Алерт!", "Медиа сервер недоступен!", Intent(), "ControllerAlive_2")
                    else
                        ToolsThreads.main(10000) { checkServerMedia(tryCount - 1) }
                }
                .send(apiMedia)
    }

    private fun checkPush() {
        val time = ToolsStorage.getLong("ControllerAlive.push", 0)
        if (time < System.currentTimeMillis() - 1000L * 60 * 60) {
            info("ControllerAlive", "Check push ERROR")
            ControllerNotifications.chanelOther.post(R.drawable.logo_campfire_alpha_black_and_white_no_margins, "Алерт!", "Пуши не работают. Последнйи пуш: ${ToolsDate.dateToString(time)}", Intent(), "ControllerAlive_2")
        } else {
            info("ControllerAlive", "Check push done")
        }
    }

}
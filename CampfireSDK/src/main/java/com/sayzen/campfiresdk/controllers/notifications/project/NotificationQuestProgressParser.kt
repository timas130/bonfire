package com.sayzen.campfiresdk.controllers.notifications.project

import android.content.Intent
import com.dzen.campfire.api.models.notifications.project.NotificationQuestProgress
import com.sayzen.campfiresdk.controllers.ControllerNotifications

public class NotificationQuestProgressParser(override val n: NotificationQuestProgress) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {

    }

    override fun asString(html: Boolean) = ""

    override fun canShow() = false

    override fun doAction() {
    }

}

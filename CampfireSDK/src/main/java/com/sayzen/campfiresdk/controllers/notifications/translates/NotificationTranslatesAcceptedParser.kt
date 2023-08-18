package com.sayzen.campfiresdk.controllers.notifications.translates

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.translates.NotificationTranslatesAccepted
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.screens.translates.STranslates
import com.sup.dev.android.libs.screens.navigator.Navigator

public class NotificationTranslatesAcceptedParser(override val n: NotificationTranslatesAccepted) : ControllerNotifications.Parser(n) {
    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        return t(API_TRANSLATE.translates_notification_translate_key, n.key)
    }

    override fun getTitle(): String {
        return tCap(API_TRANSLATE.translates_notification_accepted)
    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        Navigator.to(STranslates())
    }

}

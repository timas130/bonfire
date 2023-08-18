package com.sayzen.campfiresdk.controllers.notifications.account

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.account.NotificationPunishmentRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.screens.notifications.SNotifications
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources

public class NotificationPunishmentRemoveParser(override val n: NotificationPunishmentRemove) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, title, text, intent, tag)
    }

    override fun asString(html: Boolean) = tCap(
            API_TRANSLATE.notification_punishment_remove, n.fromAccountName, ToolsResources.sex(
            n.fromAccountSex,
            t(API_TRANSLATE.he_remove),
            t(API_TRANSLATE.she_remove)
    )
    )

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        if (Navigator.getCurrent() !is SNotifications) SNotifications.instance(Navigator.TO)
    }

}
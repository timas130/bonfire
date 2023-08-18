package com.sayzen.campfiresdk.controllers.notifications.translates

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.translates.NotificationTranslatesRejected
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

public class NotificationTranslatesRejectedParser(override val n: NotificationTranslatesRejected) : ControllerNotifications.Parser(n) {
    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val text = t(API_TRANSLATE.translates_notification_translate_key, n.key)
        val comment = if (!html) n.comment else "^${n.comment}^"
        return text + (if (ToolsText.empty(n.comment)) "" else "\n"+t(API_TRANSLATE.moderation_notification_moderator_comment) + " " + comment)
    }

    override fun getTitle(): String {
        return tCap(API_TRANSLATE.translates_notification_rejected, n.adminName, ToolsResources.sex(n.adminSex, t(API_TRANSLATE.he_reject), t(API_TRANSLATE.she_reject)))
    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        SProfile.instance(n.adminId, Navigator.TO)
    }

}

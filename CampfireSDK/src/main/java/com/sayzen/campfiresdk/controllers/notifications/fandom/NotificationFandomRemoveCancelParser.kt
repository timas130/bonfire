package com.sayzen.campfiresdk.controllers.notifications.fandom

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.fandom.NotificationFandomRemoveCancel
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText


public class NotificationFandomRemoveCancelParser(override val n: NotificationFandomRemoveCancel) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, title, text, intent, tag)
    }


    override fun asString(html: Boolean): String {
        val comment = if (!html) n.comment else "^${n.comment}^"
        return (if (ToolsText.empty(n.comment)) "" else t(API_TRANSLATE.moderation_notification_moderator_comment) + " " + comment)
    }


    override fun getTitle(): String {
        return tCap(
                API_TRANSLATE.notifications_fandom_remove_cancel, ControllerLinks.linkToAccount(n.adminName), ToolsResources.sex(
                n.adminSex,
                t(API_TRANSLATE.he_canceled),
                t(API_TRANSLATE.she_canceled)
        )
        )
    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        SFandom.instance(n.fandomId, n.languageId, Navigator.TO)
    }

}
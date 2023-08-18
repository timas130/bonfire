package com.sayzen.campfiresdk.controllers.notifications.post

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.post.NotificationModerationPostClosedNo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

public class NotificationModerationPostClosedNoParser(override val n: NotificationModerationPostClosedNo) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val comment = if (!html) n.comment else  "^${n.comment}^"
        return (if (ToolsText.empty(n.comment)) "" else t(API_TRANSLATE.moderation_notification_moderator_comment) + " " + comment)

    }

    override fun getTitle(): String {
        return tCap(API_TRANSLATE.notifications_moderation_post_close_no, n.moderatorName, ToolsResources.sex(
                n.moderatorSex,
                t(API_TRANSLATE.he_open),
                t(API_TRANSLATE.she_open)
        )
        )
    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        SModerationView.instance(n.moderationId, Navigator.TO)
    }

}
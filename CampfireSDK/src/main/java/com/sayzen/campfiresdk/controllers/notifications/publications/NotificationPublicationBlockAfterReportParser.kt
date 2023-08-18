package com.sayzen.campfiresdk.controllers.notifications.publications

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationBlockAfterReport
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText

public class NotificationPublicationBlockAfterReportParser(override val n: NotificationPublicationBlockAfterReport) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val comment = if (!html) n.comment else "^${n.comment}^"
        return (""
                + (if (n.blockAccountDate > 0) " " + t(API_TRANSLATE.moderation_notification_account_is_blocked,
                ToolsDate.dateToString(n.blockAccountDate)
        ) else "")
                + if (ToolsText.empty(n.comment)) "" else " " + t(API_TRANSLATE.moderation_notification_moderator_comment) + " " + comment)
    }

    override fun getTitle(): String {
        return tCap(API_TRANSLATE.moderation_notification_publication_is_blocked_by_report)
    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        SModerationView.instance(n.moderationId, Navigator.TO)
    }

}

package com.sayzen.campfiresdk.controllers.notifications.publications

import android.content.Intent
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationBlock
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

public class NotificationPublicationBlockParser(override val n: NotificationPublicationBlock) : ControllerNotifications.Parser(n) {

    companion object{
        var CAN_SHOW:(NotificationPublicationBlock)->Boolean= {ControllerSettings.notificationsOther}
        var DO_ACTION:(NotificationPublicationBlock)->Unit={ SModerationView.instance(it.moderationId, Navigator.TO)}
    }
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
        return tCap(if (n.blockLastPublications) API_TRANSLATE.moderation_notification_publications_is_blocked else API_TRANSLATE.moderation_notification_publication_is_blocked)
    }

    override fun canShow() = CAN_SHOW.invoke(n)
    override fun doAction() { DO_ACTION.invoke(n) }

}

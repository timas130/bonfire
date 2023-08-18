package com.sayzen.campfiresdk.controllers.notifications.rubrics

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.rubrics.NotificationRubricsMoveFandom
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.libs.text_format.TextFormatter

class NotificationRubricsMoveFandomParser(override val n: NotificationRubricsMoveFandom) : ControllerNotifications.Parser(n) {
    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        val channel = (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient)
        channel.post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        return if (n.comment.isBlank()) "" else {
            val comment = if (!html) TextFormatter(n.comment).parseNoTags()
            else "^${TextFormatter(n.comment).parseNoTags()}^"
            t(API_TRANSLATE.moderation_notification_moderator_comment) + " " + comment
        }
    }

    override fun getTitle(): String {
        return tCap(
            API_TRANSLATE.notification_rubric_move_fandom, n.adminName,
            ToolsResources.sex(n.adminSex, t(API_TRANSLATE.he_move), t(API_TRANSLATE.she_move)),
            n.rubricName, n.srcFandomName, n.destFandomName
        )
    }

    override fun canShow(): Boolean = ControllerSettings.notificationsOther

    override fun doAction() {
        SModerationView.instance(n.moderationId, Navigator.TO)
    }
}
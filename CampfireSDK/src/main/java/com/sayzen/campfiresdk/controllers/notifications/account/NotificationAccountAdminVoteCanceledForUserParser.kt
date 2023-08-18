package com.sayzen.campfiresdk.controllers.notifications.account

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.account.NotificationAccountAdminVoteCanceledForUser
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.screens.notifications.SNotifications
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

public class NotificationAccountAdminVoteCanceledForUserParser(override val n: NotificationAccountAdminVoteCanceledForUser) :
    ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {

        var text = ControllerAdminVote.getActionText(n.mAdminVote, html)

        val comment = if (!html) n.comment else "^${n.comment}^"
        text += (if (ToolsText.empty(n.comment)) "" else "\n"+t(API_TRANSLATE.moderation_notification_moderator_comment) + " " + comment)

        return text
    }

    override fun getTitle(): String {
        return tCap(
            API_TRANSLATE.notification_admin_vote_canceled_for_user,
            n.cancelAdminAccount.name,
            ToolsResources.sex(n.cancelAdminAccount.sex, t(API_TRANSLATE.he_reject), t(API_TRANSLATE.she_reject)),
            n.actionAdminAccount.name,
        )
    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        if (Navigator.getCurrent() !is SNotifications) SNotifications.instance(Navigator.TO)
    }

}
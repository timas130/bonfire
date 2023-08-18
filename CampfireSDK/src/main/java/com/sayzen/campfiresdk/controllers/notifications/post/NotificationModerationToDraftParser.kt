package com.sayzen.campfiresdk.controllers.notifications.post

import android.content.Intent
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.post.NotificationModerationToDraft
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

public class NotificationModerationToDraftParser(override val n: NotificationModerationToDraft) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val comment = if (!html) n.comment else "^${n.comment}^"
        return (if (ToolsText.empty(n.comment)) "" else t(API_TRANSLATE.moderation_notification_moderator_comment) + " " + comment)

    }

    override fun getTitle(): String {
        return if (n.publicationTyoe == API.PUBLICATION_TYPE_POST) {
            tCap(
                API_TRANSLATE.notifications_moderation_to_drafts,
                n.moderatorName,
                ToolsResources.sex(n.moderatorSex, t(API_TRANSLATE.he_return), t(API_TRANSLATE.she_return)),
                ControllerPublications.getMaskForPost(n.maskText, n.maskPageType)
            )
        } else {
            tCap(
                API_TRANSLATE.notifications_moderation_to_drafts_quest,
                n.moderatorName,
                ToolsResources.sex(n.moderatorSex, t(API_TRANSLATE.he_return), t(API_TRANSLATE.she_return)),
                ControllerPublications.getMaskForPost(n.maskText, n.maskPageType)
            )
        }
    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        if (n.moderationId > 0) SModerationView.instance(n.moderationId, Navigator.TO)
    }


}

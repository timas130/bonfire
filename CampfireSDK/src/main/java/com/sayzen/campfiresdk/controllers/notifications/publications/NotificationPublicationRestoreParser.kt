package com.sayzen.campfiresdk.controllers.notifications.publications

import android.content.Intent
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationRestore
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.screens.notifications.SNotifications
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

public class NotificationPublicationRestoreParser(override val n: NotificationPublicationRestore) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val comment = if (!html) n.comment else "^${n.comment}^"
        return if (ToolsText.empty(n.comment)) "" else t(API_TRANSLATE.moderation_notification_moderator_comment) + " " + comment
    }

    override fun getTitle(): String {
        return tCap(API_TRANSLATE.notification_admin_moderation_restore)
    }

    override fun canShow() =  ControllerSettings.notificationsOther

    override fun doAction() {
        doActionNotificationPublicationRestore(n)
    }
    private fun doActionNotificationPublicationRestore(n: NotificationPublicationRestore) {
        var publicationId = n.publicationId
        var publicationType = n.publicationType
        var toCommentId = 0L

        if (n.publicationType == API.PUBLICATION_TYPE_COMMENT) {
            publicationId = n.parentPublicationId
            publicationType = n.parentPublicationType
            toCommentId = n.publicationId
        }

        if (publicationType != 0L) ControllerPublications.toPublication(publicationType, publicationId, toCommentId)
        else SNotifications.instance(Navigator.TO)
    }



}
package com.sayzen.campfiresdk.controllers.notifications.publications

import android.content.Intent
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationReaction
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sup.dev.android.tools.ToolsResources

public class NotificationPublicationReactionParser(override val n: NotificationPublicationReaction) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean) = ""

    override fun getTitle():String{
        return tCap(
                if (n.publicationType == API.PUBLICATION_TYPE_COMMENT) API_TRANSLATE.notification_reaction_comment
                else API_TRANSLATE.notification_reaction_message,
                n.accountName, ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_react), t(API_TRANSLATE.she_react)),
                ControllerPublications.getMaskFor(n.publicationType, n.maskText, n.maskPageType)
        )
    }


    override fun canShow() = ControllerSettings.notificationsCommentsAnswers

    override fun doAction() {
        if (n.publicationType == API.PUBLICATION_TYPE_COMMENT) ControllerPublications.toPublication(n.parentPublicationType, n.parentPublicationId, n.publicationId)
        else  ControllerPublications.toPublication(n.publicationType, n.publicationId)
    }


}
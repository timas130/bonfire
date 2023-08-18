package com.sayzen.campfiresdk.controllers.notifications.publications

import android.content.Intent
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.publications.NotificationMention
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources

public class NotificationMentionParser(override val n: NotificationMention) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean) = n.text

    override fun getTitle() = tCap(API_TRANSLATE.notification_mention, n.fromAccountName, ToolsResources.sex(n.fromAccountSex, t(API_TRANSLATE.he_mentioned),t(API_TRANSLATE.she_mentioned)))

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        doActionNotificationMention(n)
    }

    private fun doActionNotificationMention(n: NotificationMention) {
        if (n.publicationType == API.PUBLICATION_TYPE_COMMENT) {
            ControllerPublications.toPublication(n.tag2, n.tag1, n.publicationId)
        }
        if (n.publicationType == API.PUBLICATION_TYPE_CHAT_MESSAGE) {
            SChat.instance(n.publicationId, true, Navigator.TO)
        }
        if (n.publicationType == API.PUBLICATION_TYPE_POST) {
            SPost.instance(n.publicationId, Navigator.TO)
        }
    }

}
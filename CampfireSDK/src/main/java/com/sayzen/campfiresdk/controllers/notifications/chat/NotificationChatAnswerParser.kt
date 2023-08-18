package com.sayzen.campfiresdk.controllers.notifications.chat

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.notifications.chat.NotificationChatAnswer
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources


public class NotificationChatAnswerParser(override val n: NotificationChatAnswer) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        NotificationChatMessageParser.sendNotification(n.tag, sound)
    }

    override fun asString(html: Boolean): String {
        return if (n.publicationChatMessage.resourceId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creator.name + ": " + t(API_TRANSLATE.app_image)
        else if (n.publicationChatMessage.stickerId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creator.name + ": " + t(API_TRANSLATE.app_sticker)
        else n.publicationChatMessage.creator.name + ": " + n.publicationChatMessage.text
    }

    override fun canShow() = canShowNotificationChatAnswer(n)

    override fun doAction() {
        doActionNotificationChatAnswer(n)
    }

    private fun canShowNotificationChatAnswer(n: NotificationChatAnswer): Boolean {
        if (!ControllerSettings.notificationsChatAnswers) return false
        if (SupAndroid.activityIsVisible && Navigator.getCurrent() is SChat) {
            val screen = Navigator.getCurrent() as SChat
            return screen.chat.tag != n.tag
        } else {
            return true
        }
    }

    private fun doActionNotificationChatAnswer(n: NotificationChatAnswer) {
        SChat.instance(ChatTag(n.publicationChatMessage.chatType, n.publicationChatMessage.fandom.id, n.publicationChatMessage.fandom.languageId), n.publicationChatMessage.id, true, Navigator.TO)

    }
}
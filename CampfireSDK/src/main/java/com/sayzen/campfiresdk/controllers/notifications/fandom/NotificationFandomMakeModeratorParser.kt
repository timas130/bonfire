package com.sayzen.campfiresdk.controllers.notifications.fandom

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.fandom.NotificationFandomMakeModerator
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator


public class NotificationFandomMakeModeratorParser(override val n: NotificationFandomMakeModerator) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, title, text, intent, tag)
    }

    override fun asString(html: Boolean) =
            tCap(API_TRANSLATE.notifications_fandom_make_moderator, n.fandomName)

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        SFandom.instance(n.fandomId, n.languageId, Navigator.TO)
    }

}
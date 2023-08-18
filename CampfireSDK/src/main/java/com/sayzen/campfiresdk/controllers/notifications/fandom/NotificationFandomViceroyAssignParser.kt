package com.sayzen.campfiresdk.controllers.notifications.fandom

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.fandom.NotificationFandomViceroyAssign
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

public class NotificationFandomViceroyAssignParser(override val n: NotificationFandomViceroyAssign) :
        ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, title, text, intent, tag)
    }

    override fun getTitle(): String {
        return  if (ToolsText.empty(n.comment)) "" else " " +
                t(API_TRANSLATE.notifications_fandom_viceroy_assign, n.adminAccountName, ToolsResources.sex(n.adminAccountSex, t(API_TRANSLATE.he_assign), t(API_TRANSLATE.she_assign)), n.fandomName)
    }

    override fun asString(html: Boolean): String {
        val comment = if (!html) n.comment else "^${n.comment}^"
        return t(API_TRANSLATE.app_comment) + ": " + comment
    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        SFandom.instance(n.fandomId, n.languageId, Navigator.TO)
    }
}

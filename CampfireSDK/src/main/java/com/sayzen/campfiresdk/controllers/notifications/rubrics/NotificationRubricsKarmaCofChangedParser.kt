package com.sayzen.campfiresdk.controllers.notifications.rubrics

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.rubrics.NotificationRubricsKarmaCofChanged
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricPosts
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

public class NotificationRubricsKarmaCofChangedParser(override val n: NotificationRubricsKarmaCofChanged) : ControllerNotifications.Parser(n) {
    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val text_cof = if (!html) ToolsText.numToStringRound(n.cofChange / 100.0, 2) else "{" + (if (n.cofChange < 0) "red " else "green ") + ToolsText.numToStringRound(n.cofChange / 100.0, 2) + "}"
        val text_value = if (!html) ToolsText.numToStringRound(n.newCof / 100.0, 2) else "{" + (if (n.newCof < 0) "red " else "green ") + ToolsText.numToStringRound(n.newCof / 100.0, 2) + "}"
        return t(API_TRANSLATE.notification_rubric_cof_text, text_cof, text_value)
    }

    override fun getTitle(): String {
        return tCap(API_TRANSLATE.notification_rubric_cof_title, n.rubricName)
    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        SRubricPosts.instance(n.rubricId, Navigator.TO)
    }
}
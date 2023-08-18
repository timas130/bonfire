package com.sayzen.campfiresdk.controllers.notifications.account


import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.account.NotificationEffectRemove
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.notifications.SNotifications
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

public class NotificationEffectRemoveParser(override val n: NotificationEffectRemove) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val comment = if (!html) n.comment else "^${n.comment}^"
        return (if (ToolsText.empty(n.comment)) "" else t(API_TRANSLATE.moderation_notification_moderator_comment) + " " + comment)
    }
    override fun getTitle(): String {
        return tCap(
                API_TRANSLATE.effect_notif_remove,
                n.adminName,
                ToolsResources.sex(n.adminSex, t(API_TRANSLATE.he_removed_effect), t(API_TRANSLATE.she_removed_effect)),
                ControllerEffects.getTitle(n.effectIndex)
        )
    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        SProfile.instance(ControllerApi.account.getId(), Navigator.TO)
    }

}
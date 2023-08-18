package com.sayzen.campfiresdk.controllers.notifications.account

import android.content.Intent
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.account.NotificationEffectAdd
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.notifications.SNotifications
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

public class NotificationEffectAddParser(override val n: NotificationEffectAdd) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val comment = if (!html) n.mAccEffect.comment else "^${n.mAccEffect.comment}^"
        return (if (ToolsText.empty(n.mAccEffect.comment)) "" else t(API_TRANSLATE.moderation_notification_moderator_comment) + " " + comment)
    }
    override fun getTitle(): String {

        if(n.mAccEffect.tag == API.EFFECT_TAG_SOURCE_SYSTEM){
            return tCap(API_TRANSLATE.effect_notif_add_system, ControllerEffects.getTitle(n.mAccEffect))
        }else{
            return tCap(
                    API_TRANSLATE.effect_notif_add_admin,
                    n.adminName,
                    ToolsResources.sex(n.adminSex, t(API_TRANSLATE.he_applied), t(API_TRANSLATE.she_applied)),
                    ControllerEffects.getTitle(n.mAccEffect)
            )
        }


    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        SProfile.instance(n.mAccEffect.accountId, Navigator.TO)
    }


}
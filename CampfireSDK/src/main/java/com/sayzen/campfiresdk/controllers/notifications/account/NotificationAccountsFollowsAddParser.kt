package com.sayzen.campfiresdk.controllers.notifications.account

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.account.NotificationAccountsFollowsAdd
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources

public class NotificationAccountsFollowsAddParser(override val n: NotificationAccountsFollowsAdd) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun getTitle(): String {
        return t(API_TRANSLATE.app_profile)
    }

    override fun asString(html: Boolean) = tCap(
            API_TRANSLATE.notification_profile_follows_add, n.accountName, ToolsResources.sex(
            n.accountSex,
            t(API_TRANSLATE.he_subscribed),
            t(API_TRANSLATE.she_subscribed)
    )
    )

    override fun canShow() = ControllerSettings.notificationsFollows

    override fun doAction() {
        SProfile.instance(n.accountId, Navigator.TO)
    }

}

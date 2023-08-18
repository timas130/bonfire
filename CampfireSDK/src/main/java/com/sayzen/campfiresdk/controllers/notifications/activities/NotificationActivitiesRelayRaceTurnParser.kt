package com.sayzen.campfiresdk.controllers.notifications.activities

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesRelayRaceTurn
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.screens.activities.user_activities.relay_race.SRelayRaceInfo
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources

public class NotificationActivitiesRelayRaceTurnParser(override val n: NotificationActivitiesRelayRaceTurn) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        if (n.fromAccountId > 0) return tCap(API_TRANSLATE.notification_activities_relay_race_turn, n.fromAccountName, ToolsResources.sex(n.fromAccountSex, t(API_TRANSLATE.he_give), t(API_TRANSLATE.she_give)), n.activityName, n.fandomName)
        return tCap(API_TRANSLATE.notification_activities_relay_race_turn_from_system, n.activityName, n.fandomName)
    }

    override fun getTitle() = t(API_TRANSLATE.app_relay_race)

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        SRelayRaceInfo.instance(n.activityId, Navigator.TO)
    }


}
package com.sayzen.campfiresdk.controllers

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesRelayRaceTurn
import com.dzen.campfire.api.requests.activities.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.activities.*
import com.sayzen.campfiresdk.models.events.fandom.EventFandomAccepted
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.screens.activities.user_activities.SRelayRaceCreate
import com.sayzen.campfiresdk.screens.activities.user_activities.SplashReject
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerActivities {

    private val eventBus = EventBus
            .subscribe(EventFandomAccepted::class) { setFandomsCount(suggestedFandomsCount - 1) }
            .subscribe(EventNotification::class) { if (it.notification is NotificationActivitiesRelayRaceTurn) reloadActivities() }
            .subscribe(EventActivitiesRelayRaceRejected::class) { setRelayRacesCount(relayRacesCount - 1) }

    fun init() {

    }


    fun clear() {
        clearUser()
        clearAdmins()
    }


    //
    //  User
    //

    private var relayRacesCount = 0L
    private var rubricsCount = 0L

    fun setRelayRacesCount(relayRacesCount: Long) {
        this.relayRacesCount = relayRacesCount
        EventBus.post(EventActivitiesCountChanged())
    }

    fun setRubricsCount(rubricsCount: Long) {
        this.rubricsCount = rubricsCount
        EventBus.post(EventActivitiesCountChanged())
    }

    fun clearUser() {
        relayRacesCount = 0L
        rubricsCount = 0L
        EventBus.post(EventActivitiesCountChanged())
    }

    fun getActivitiesCount() = getRelayRacesCount() + getRubricsCount()

    fun getRelayRacesCount() = relayRacesCount
    fun getRubricsCount() = rubricsCount

    fun showMenu(userActivity: UserActivity, view: View) {
        SplashMenu()
                .add(t(API_TRANSLATE.app_subscription)) { subscribtion(userActivity) }
                .add(t(API_TRANSLATE.app_copy_link)) { ToolsAndroid.setToClipboard(ControllerLinks.linkToActivity(userActivity.id));ToolsToast.show(t(API_TRANSLATE.app_copied)) }
                .spoiler(t(API_TRANSLATE.app_moderator))
                .add(t(API_TRANSLATE.app_change)) { Navigator.to(SRelayRaceCreate(userActivity)) }.condition(ControllerApi.can(userActivity.fandom.id, userActivity.fandom.languageId, API.LVL_MODERATOR_RELAY_RACE)).textColorRes(R.color.white).backgroundRes(R.color.blue_700)
                .add(t(API_TRANSLATE.app_remove)) { removeActivity(userActivity) }.condition(ControllerApi.can(userActivity.fandom.id, userActivity.fandom.languageId, API.LVL_MODERATOR_RELAY_RACE)).textColorRes(R.color.white).backgroundRes(R.color.blue_700)
                .asPopupShow(view)
    }

    fun subscribtion(userActivity: UserActivity){
        ApiRequestsSupporter.executeProgressDialog(RActivitiesSubscribeGet(userActivity.id)){ r->
            ApiRequestsSupporter.executeEnabledConfirm(
                    if(r.subscribed) t(API_TRANSLATE.activities_unsubscribe_alert) else t(API_TRANSLATE.activities_subscribe_alert),
                    if(r.subscribed) t(API_TRANSLATE.app_unsubscribe) else  t(API_TRANSLATE.app_subscribe),
                    RActivitiesSubscribe(userActivity.id, !r.subscribed)
            ){
                ToolsToast.show(t(API_TRANSLATE.app_done))
            }
        }
    }

    fun removeActivity(userActivity: UserActivity) {
        ControllerApi.moderation(t(API_TRANSLATE.activities_relay_race_remove_title), t(API_TRANSLATE.app_remove), { RActivitiesRemove(userActivity.id, it) }) {
            ToolsToast.show(t(API_TRANSLATE.app_done))
            EventBus.post(EventActivitiesRemove(userActivity.id))
            reloadActivities()
        }
    }

    //
    //  Administration
    //

    private var administrationLoadInProgress = false
    private var suggestedFandomsCount = 0L
    private var reportsCount = 0L
    private var reportsUserCount = 0L
    private var blocksCount = 0L
    private var translateModerationCount = 0L
    private var adminVoteCount = 0L

    fun setFandomsCount(count: Long) {
        suggestedFandomsCount = count
        EventBus.post(EventActivitiesAdminCountChanged())
    }


    fun getSuggestedFandomsCount() = suggestedFandomsCount
    fun getReportsCount() = reportsCount
    fun getReportsUserCount() = reportsUserCount
    fun getBlocksCount() = blocksCount
    fun getTranslatesCount() = API_TRANSLATE.map.size - (ControllerTranslate.getMyMap()?.size?:0)
    fun getTranslatesModerationCount() = translateModerationCount
    fun getAdminVoteCount() = adminVoteCount
    fun isAdministrationLoadInProgress() = administrationLoadInProgress

    fun clearAdmins() {
        suggestedFandomsCount = 0
        reportsCount = 0
        reportsUserCount = 0
        blocksCount = 0
        adminVoteCount = 0
        translateModerationCount = 0
        EventBus.post(EventActivitiesAdminCountChanged())
    }

    //
    //  Reload
    //

    fun reloadActivities() {
        administrationLoadInProgress = true
        clearAdmins()
        RActivitiesGetCounts(ControllerSettings.adminReportsLanguages)
                .onComplete {
                    setRelayRacesCount(it.relayRacesCount)
                    setRubricsCount(it.rubricsCount)
                    suggestedFandomsCount = it.suggestedFandomsCount
                    reportsCount = it.reportsCount
                    reportsUserCount = it.reportsUserCount
                    blocksCount = it.blocksCount
                    translateModerationCount = it.translateModerationCount
                    adminVoteCount = it.adminVoteCount
                    administrationLoadInProgress = false
                    EventBus.post(EventActivitiesAdminCountChanged())
                }
                .onError {
                    administrationLoadInProgress = false
                    EventBus.post(EventActivitiesAdminCountChanged())
                    err(it)
                }
                .send(api)
    }

    //
    //  Relay race
    //

    fun reject(userActivityId: Long) {
        SplashReject(userActivityId).asSheetShow()
    }

    fun member(userActivityId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.activities_relay_race_member_text), t(API_TRANSLATE.app_participate), RActivitiesRelayRaceMember(userActivityId, true)) { r ->
            ToolsToast.show(t(API_TRANSLATE.app_done))
            EventBus.post(EventActivitiesRelayRaceMemberStatusChanged(userActivityId, 1, r.myIsCurrentMember))
        }
    }

    fun no_member(userActivityId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.activities_relay_race_member_text_no), t(API_TRANSLATE.app_participate_no), RActivitiesRelayRaceMember(userActivityId, false)) { r ->
            ToolsToast.show(t(API_TRANSLATE.app_done))
            setRelayRacesCount(getActivitiesCount() - 1)
            EventBus.post(EventActivitiesRelayRaceMemberStatusChanged(userActivityId, 0, false))
        }
    }


}
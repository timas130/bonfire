package com.sayzen.campfiresdk.screens.notifications

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.Notification
import com.dzen.campfire.api.requests.accounts.RAccountsNotificationsGetAll
import com.dzen.campfire.api.requests.accounts.RAccountsNotificationsView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.notifications.EventNotificationsCountChanged
import com.sup.dev.android.app.SupAndroid
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.splash.SplashCheckBoxes
import com.dzen.campfire.api.tools.client.Request
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.libs.eventBus.EventBus

class SNotifications private constructor() : SLoadingRecycler<CardNotification, Notification>() {

    companion object {

        fun instance(action: NavigationAction) {
            Navigator.reorderOrCreate(SNotifications::class){
                SNotifications()
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventNotification::class) { this.onNotification(it) }

    private var subscriptionX: Request<*>? = null
    private var autoRead = false

    init {
        disableShadows()
        isSingleInstanceInBackStack = true
        setTitle(t(API_TRANSLATE.app_notifications))
        setTextEmpty(t(API_TRANSLATE.notifications_empty))
        setTextProgress(t(API_TRANSLATE.notifications_loading))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_6)
        vFab.setImageResource(R.drawable.ic_done_all_white_24dp)
        vFab.setOnClickListener {
            ControllerNotifications.removeNotificationFromNewAll()
            ToolsToast.show(t(API_TRANSLATE.app_done))
            ApiRequestsSupporter.execute(RAccountsNotificationsView(emptyArray(), emptyArray())) {

            }
        }
        addToolbarIcon(R.drawable.ic_tune_white_24dp) { showFilters() }

        adapter.setBottomLoader { onLoad, cards ->
            subscriptionX = RAccountsNotificationsGetAll(
                    if (cards.isEmpty()) 0 else cards[cards.size - 1].notification.dateCreate,
                    getFilters(!ControllerSettings.filterOther), ControllerSettings.filterOther)
                    .onComplete { r ->
                        for (n in r.notifications) {
                            if (n.status == 0L) ControllerNotifications.addNewNotifications(n)
                            else if (n.status == 1L) ControllerNotifications.removeNotificationFromNew(n.id)
                        }
                        onLoad.invoke(r.notifications)
                        if (Navigator.getCurrent() == this && SupAndroid.activityIsVisible)
                            ControllerNotifications.hide(ControllerNotifications.TYPE_NOTIFICATIONS)

                        (vFab as View).visibility = if (ControllerNotifications.getNewNotificationsCount() == 0) View.GONE else View.VISIBLE
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
        adapter.setRetryMessage(t(API_TRANSLATE.error_network), t(API_TRANSLATE.app_retry))

    }

    override fun classOfCard() = CardNotification::class

    override fun map(item: Notification) = CardNotification(this, item)

    override fun onDestroy() {
        super.onDestroy()
        readNow()
    }

    override fun onStackChanged() {
        super.onStackChanged()
        readNow()
    }

    private fun readNow() {
        if (!autoRead) {
            autoRead = true
            if (ControllerSettings.autoReadNotifications) {
                ControllerNotifications.removeNotificationFromNewAll()
            } else {
                //  Читаем отключеные уведомления
                ControllerNotifications.removeNotificationFromNew(getFilters(false))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ControllerNotifications.hide(ControllerNotifications.TYPE_NOTIFICATIONS)
    }

    private fun onNotification(e: EventNotification) {
        if (!e.notification.isShadow() && ControllerNotifications.canShowByFilter(e.notification)) {
            adapter.addWithHashTop(map(e.notification))
        }
    }

    override fun reload() {
        if (subscriptionX != null) subscriptionX!!.unsubscribe()
        adapter.reloadBottom()
    }

    //
    //  Filters
    //

    private fun showFilters() {
        val oldFollowsPublications = ControllerSettings.filterFollowsPublications
        val oldFollows = ControllerSettings.filterFollows
        val oldAchievements = ControllerSettings.filterAchievements
        val oldComments = ControllerSettings.filterComments
        val oldKarma = ControllerSettings.filterKarma
        val oldAnswers = ControllerSettings.filterAnswers
        val oldImportant = ControllerSettings.filterImportant
        val oldOther = ControllerSettings.filterOther

        SplashCheckBoxes()
                .add(t(API_TRANSLATE.settings_notifications_filter_follows_publications)).onChange { ControllerSettings.filterFollowsPublications = it.isChecked }.checked(ControllerSettings.filterFollowsPublications)
                .add(t(API_TRANSLATE.settings_notifications_filter_follows)).onChange { ControllerSettings.filterFollows = it.isChecked }.checked(ControllerSettings.filterFollows)
                .add(t(API_TRANSLATE.app_achievements)).onChange {  ControllerSettings.filterAchievements = it.isChecked }.checked(ControllerSettings.filterAchievements)
                .add(t(API_TRANSLATE.settings_notifications_filter_comments)).onChange { ControllerSettings.filterComments = it.isChecked }.checked(ControllerSettings.filterComments)
                .add(t(API_TRANSLATE.settings_notifications_filter_answers)).onChange { ControllerSettings.filterAnswers = it.isChecked }.checked(ControllerSettings.filterAnswers)
                .add(t(API_TRANSLATE.settings_notifications_filter_karma)).onChange {  ControllerSettings.filterKarma = it.isChecked }.checked(ControllerSettings.filterKarma)
                .add(t(API_TRANSLATE.settings_notifications_filter_important)).onChange { ControllerSettings.filterImportant = it.isChecked }.checked(ControllerSettings.filterImportant)
                .add(t(API_TRANSLATE.settings_notifications_filter_app_other)).onChange { ControllerSettings.filterOther = it.isChecked }.checked(ControllerSettings.filterOther)
                .setOnHide {
                    if (oldFollowsPublications != ControllerSettings.filterFollowsPublications
                            || oldFollows != ControllerSettings.filterFollows
                            || oldAchievements != ControllerSettings.filterAchievements
                            || oldComments != ControllerSettings.filterComments
                            || oldKarma != ControllerSettings.filterKarma
                            || oldAnswers != ControllerSettings.filterAnswers
                            || oldImportant != ControllerSettings.filterImportant
                            || oldOther != ControllerSettings.filterOther
                    ) {
                        EventBus.post(EventNotificationsCountChanged())
                        reload()
                    }
                }
                .asSheetShow()
    }

    private fun getFilters(on: Boolean): Array<Long> {
        val list = ArrayList<Long>()
        if (on == ControllerSettings.filterFollowsPublications) list.add(API.NOTIF_FOLLOWS_PUBLICATION)
        if (on == ControllerSettings.filterFollows) list.add(API.NOTIF_ACCOUNT_FOLLOWS_ADD)
        if (on == ControllerSettings.filterAchievements) list.add(API.NOTIF_ACHI)
        if (on == ControllerSettings.filterComments) list.add(API.NOTIF_COMMENT)
        if (on == ControllerSettings.filterAnswers) list.add(API.NOTIF_COMMENT_ANSWER)
        if (on == ControllerSettings.filterKarma) list.add(API.NOTIF_KARMA_ADD)
        if (on == ControllerSettings.filterImportant) list.add(API.NOTIF_PUBLICATION_IMPORTANT)
        return list.toTypedArray()
    }

}
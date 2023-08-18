package com.sayzen.campfiresdk.screens.activities.user_activities

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.requests.activities.RActivitiesGetAllForAccount
import com.dzen.campfire.api.requests.activities.RActivitiesGetAllNotForAccount
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesCreate
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.CardDividerTitle
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus

class SRelayRacesList constructor(
        private val fandomId: Long = 0,
        private val languageId: Long = 0,
        private val onSelected: ((UserActivity) -> Unit)? = null
) : SLoadingRecycler<CardUserActivity, UserActivity>(R.layout.screen_activities_user_activities) {

    private val eventBus = EventBus.subscribe(EventActivitiesCreate::class) { reload() }

    private var subscribedLoaded = false
    private var lockOnEmpty = false

    init {
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_relay_races))
        setTextProgress(t(API_TRANSLATE.activities_loading))
        setTextEmpty(if (fandomId > 0) t(API_TRANSLATE.activities_empty_user) else t(API_TRANSLATE.activities_empty))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_26)

        val vFab: FloatingActionButton = findViewById(R.id.vFab)
        (vFab as View).visibility = if (ControllerApi.account.getLevel() >= API.LVL_MODERATOR_RELAY_RACE.lvl) View.VISIBLE else View.GONE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {
            Navigator.to(SRelayRaceCreate(null))
        }

        adapter
                .setBottomLoader { onLoad, cards -> load(onLoad, cards) }
                .addOnLoadedPack {
                    adapter.remove(CardSpace::class)
                    adapter.add(CardSpace(72))
                }
    }

    override fun classOfCard() = CardUserActivity::class

    override fun map(item: UserActivity): CardUserActivity {
       return if (onSelected == null) CardUserActivity(item) else CardUserActivity(item) { onSelected(it) }
    }

    private fun onSelected(userActivity: UserActivity) {
        onSelected?.invoke(userActivity)
        Navigator.remove(this)
    }

    override fun reload() {
        adapter.remove(CardDividerTitle::class)
        lockOnEmpty = true
        subscribedLoaded = false
        super.reload()
    }

    private fun load(onLoad: (Array<UserActivity>?) -> Unit, cards: ArrayList<CardUserActivity>) {
        lockOnEmpty = false
        if (!subscribedLoaded) {
            subscription = RActivitiesGetAllForAccount(ControllerApi.account.getId(), fandomId, languageId, cards.size.toLong())
                    .onComplete {
                        if (it.userActivities.isEmpty()) {
                            subscribedLoaded = true
                            if (onSelected == null) adapter.add(CardDividerTitle(t(API_TRANSLATE.activities_all)).setDividerBottom(false))
                            load(onLoad, cards)
                        }else{
                            onLoad.invoke(it.userActivities)
                        }
                    }
                    .onError { onLoad.invoke(null) }
                    .send(api)

        } else {
            if (onSelected != null) {
                onLoad.invoke(emptyArray())
                return
            }
            subscription = RActivitiesGetAllNotForAccount(ControllerApi.account.getId(), fandomId, languageId, cards.size - getMyCount())
                    .onComplete {
                        onLoad.invoke(it.userActivities)
                    }
                    .onError { onLoad.invoke(null) }
                    .send(api)


        }
    }

    private fun getMyCount(): Long {
        var count = 0L
        for (c in adapter.get(CardUserActivity::class)) if (ControllerApi.isCurrentAccount(c.userActivity.currentAccount.id) && !c.tag1IsReset) count++
        return count
    }



}

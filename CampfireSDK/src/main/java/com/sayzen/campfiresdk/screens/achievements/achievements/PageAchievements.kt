package com.sayzen.campfiresdk.screens.achievements.achievements

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.account.NotificationAchievement
import com.dzen.campfire.api.requests.achievements.RAchievementsInfo
import com.dzen.campfire.api.requests.achievements.RAchievementsPack
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.achievements.CardInfo
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.project.EventAchiProgressIncr
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.cards.CardLoading
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.java.libs.eventBus.EventBus

class PageAchievements(
        val accountId: Long,
        scrollToIndex: Long,
        val r: RAchievementsInfo.Response
) : Card(0) {

    private val eventBus = EventBus
            .subscribe(EventNotification::class) {onNotification(it) }
            .subscribe(EventAchiProgressIncr::class) { onEventAchiProgressIncr(it) }

    private val adapterSub: RecyclerCardAdapter = RecyclerCardAdapter()
    private val cardInfo: CardInfo = CardInfo(t(API_TRANSLATE.achi_karma_hint), t(API_TRANSLATE.app_level), r.karmaForce, true, CampfireConstants.getLvlImage(r.karmaForce))
    private var scrollToIndex: Int = 0

    private val indexes = ArrayList<Long>()
    private val progress = ArrayList<Long>()

    init {

        adapterSub.add(cardInfo)

        val spoiler1 = CardSpoilerAchi(this, 1).setTitle(t(API_TRANSLATE.achi_spoiler_instruction))
        val spoiler2 = CardSpoilerAchi(this, 2).setTitle(t(API_TRANSLATE.achi_spoiler_sharing))
        val spoiler3 = CardSpoilerAchi(this, 3).setTitle(t(API_TRANSLATE.achi_spoiler_publications))
        val spoiler4 = CardSpoilerAchi(this, 4).setTitle(t(API_TRANSLATE.app_moderation))
        val spoiler5 = CardSpoilerAchi(this, 6).setTitle(t(API_TRANSLATE.app_viceroy))
        val spoiler6 = CardSpoilerAchi(this, 5).setTitle(t(API_TRANSLATE.achi_spoiler_other))

        adapterSub.add(spoiler1)
        adapterSub.add(spoiler2)
        adapterSub.add(spoiler3)
        adapterSub.add(spoiler4)
        adapterSub.add(spoiler5)
        adapterSub.add(spoiler6)


        if (scrollToIndex != 0L) {
            for (card in adapterSub.get(CardSpoilerAchi::class)) {
                for (achi in card.pack) {
                    if (achi.index == scrollToIndex) {
                        card.scrollToIndex = scrollToIndex
                        card.setExpanded(true)
                    }
                }
            }
        }
    }

    fun loadPack(index: Int, cardSpoiler: CardSpoilerAchi) {
        cardSpoiler.cardLoading.setState(CardLoading.State.LOADING)
        cardSpoiler.cardLoading.setOnRetry { loadPack(index, cardSpoiler) }
        RAchievementsPack(accountId, index)
                .onComplete { r ->
                    for (i in r.indexes.indices) {
                        indexes.add(r.indexes[i])
                        progress.add(r.progress[i])
                    }

                    cardSpoiler.onLoaded()
                }
                .onError {
                    cardSpoiler.cardLoading.setRetryMessage(t(API_TRANSLATE.error_network))
                    cardSpoiler.cardLoading.setRetryButton(t(API_TRANSLATE.app_retry)){loadPack(index, cardSpoiler)}
                    cardSpoiler.cardLoading.setState(CardLoading.State.RETRY)
                }
                .send(api)
    }

    fun setAchiProgress(index: Long, prog: Long) {
        for (i in 0 until indexes.size) if (indexes[i] == index) progress[i] = prog
    }

    fun getAchiProgress(index: Long): Long {
        for (i in 0 until indexes.size) if (indexes[i] == index) return progress[i]
        return 0
    }

    fun achiLvl(index: Long): Long {
        for (i in r.indexes.indices) if (r.indexes[i] == index) return r.lvls[i]
        return 0
    }

    override fun instanceView(): View {
        val v = RecyclerView(SupAndroid.activity!!)
        v.layoutManager = LinearLayoutManager(SupAndroid.activity)
        v.id = R.id.vRecycler
        ToolsView.setRecyclerAnimation(v)
        return v
    }

    override fun bindView(view: View) {
        val vRecycler = view as RecyclerView
        vRecycler.adapter = adapterSub
        if (this.scrollToIndex > 0) vRecycler.smoothScrollToPosition(this.scrollToIndex)
    }

    fun scrollToCard(card: Card) {
        if (getView() == null) return
        val vRecycler: RecyclerView = getView()!!.findViewById(R.id.vRecycler)
        val index = adapterSub.indexOf(card)
        if(index > adapterSub.size()-3) {
            vRecycler.scrollToPosition(index)
        } else {
            vRecycler.scrollToPosition(index + 2)
        }
    }

    //
    //  EventBus
    //

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationAchievement) {
            val n = e.notification
            val l = adapterSub.get(CardAchievement::class)
            for (card in l)
                if (n.achiIndex == card.achievement.index) {
                    cardInfo.count = cardInfo.count + card.achievement.force * (n.achiLvl - card.getLvl())
                    card.setForcedLvl(n.achiLvl)
                }
        }
    }

    private fun onEventAchiProgressIncr(e: EventAchiProgressIncr) {
        setAchiProgress(e.index, getAchiProgress(e.index)+1)
        val l = adapterSub.get(CardAchievement::class)
        for (card in l) if (e.index == card.achievement.index) card.update()
    }

}

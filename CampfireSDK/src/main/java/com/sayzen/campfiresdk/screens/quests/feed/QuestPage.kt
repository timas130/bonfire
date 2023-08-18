package com.sayzen.campfiresdk.screens.quests.feed

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.requests.quests.RQuestsGetLatest
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardQuestDetails
import com.sayzen.campfiresdk.models.events.publications.EventPostStatusChange
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.views.cards.CardScreenLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus

class QuestPage(val screen: Screen) : CardScreenLoadingRecycler<CardQuestDetails, Publication>() {
    private val eventBus = EventBus
        .subscribe(EventPostStatusChange::class) {
            reload()
        }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardQuestDetails, Publication> {
        val adapterX = RecyclerCardAdapterLoading<CardQuestDetails, Publication>(CardQuestDetails::class) { pub ->
            CardQuestDetails(pub as QuestDetails)
        }
        adapterX.setShowLoadingCard(false)
        adapterX.setBottomLoader { onLoad, cards ->
            RQuestsGetLatest(
                if (cards.isEmpty()) 0 else cards[cards.size - 1].questDetails.dateCreate,
                ControllerSettings.feedLanguages
            ).onComplete { resp ->
                adapterX.setShowLoadingCard(true)
                onLoad(resp.quests)
                setTextProgress(t(API_TRANSLATE.feed_loading_35))
            }.onNetworkError {
                onLoad.invoke(null)
            }.send(api)
        }

        return adapterX
    }
}
package com.sayzen.campfiresdk.screens.activities.administration.fandoms

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.fandoms.RFandomsSuggestedGetAll
import com.sayzen.campfiresdk.controllers.ControllerActivities
import com.sayzen.campfiresdk.models.cards.CardFandom
import com.sayzen.campfiresdk.models.events.fandom.EventFandomAccepted
import com.sayzen.campfiresdk.screens.fandoms.suggest.SFandomSuggest
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus
import kotlin.reflect.KClass

class SAdministrationFandoms private constructor(

) : SLoadingRecycler<CardFandom, Fandom>() {

    companion object {

        fun instance(action: NavigationAction) {
            Navigator.action(action, SAdministrationFandoms())
        }
    }

    private val eventBus = EventBus
            .subscribe(EventFandomAccepted::class) { this.onEventFandomAccepted(it) }

    init {
        disableNavigation()
        disableShadows()

        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_2)
        setTitle(t(API_TRANSLATE.administration_fandoms))
        setTextEmpty(t(API_TRANSLATE.administration_fandoms_empty))

        adapter.setBottomLoader { onLoad, cards ->
            RFandomsSuggestedGetAll(cards.size.toLong())
                    .onComplete { r ->
                        ControllerActivities.setFandomsCount(adapter.get(CardFandom::class).size + r.fandoms.size.toLong())
                        onLoad.invoke(r.fandoms)
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardFandom::class

    override fun map(item: Fandom): CardFandom {
        val card = CardFandom(item, onClick = { SFandomSuggest.instance(item.id, Navigator.TO) }, onLongClick = {}).setShowSubscribes(false)
        card.avatarClickable = false
        return card
    }

    //
    //  EventBus
    //

    private fun onEventFandomAccepted(e: EventFandomAccepted) {
        val cardFandoms = adapter.get(CardFandom::class)
        for (c in cardFandoms)
            if (c.getFandomId() == e.fandomId)
                adapter.remove(c)
    }


}

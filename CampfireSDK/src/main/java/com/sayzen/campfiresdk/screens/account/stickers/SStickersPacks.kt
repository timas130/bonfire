package com.sayzen.campfiresdk.screens.account.stickers

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersPacksGetAllByAccount
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.stickers.CardStickersPack
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackCollectionChanged
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackCreate
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus
import kotlin.reflect.KClass

class SStickersPacks(
        val accountId: Long
) : SLoadingRecycler<CardStickersPack, PublicationStickersPack>() {

    val eventBus = EventBus
            .subscribe(EventStickersPackCreate::class) { if (ControllerApi.isCurrentAccount(accountId)) reload() }
            .subscribe(EventStickersPackCollectionChanged::class) { if (ControllerApi.isCurrentAccount(accountId)) reload() }

    init {
        disableNavigation()
        disableShadows()

        setTitle(t(API_TRANSLATE.app_stickers))
        setTextEmpty(t(API_TRANSLATE.stickers_packs_empty))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_4)
        if (accountId == ControllerApi.account.getId()) {
            if (ControllerApi.can(API.LVL_CREATE_STICKERS)) {
                addToolbarIcon(R.drawable.ic_add_white_24dp) {
                    Navigator.to(SStickersPackCreate(null))
                }
            }
            (vFab as View).visibility = View.VISIBLE
            vFab.setImageResource(R.drawable.ic_search_white_24dp)
            vFab.setOnClickListener {
                Navigator.to(SStickersPacksSearch())
            }
        }

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RStickersPacksGetAllByAccount(accountId, if (cards.isEmpty()) 0 else cards.get(cards.size - 1).xPublication.publication.dateCreate)
                    .onComplete { r -> onLoad.invoke(r.stickersPacks) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }

        adapter.add(CardFavorites(accountId))
    }

    override fun classOfCard() = CardStickersPack::class

    override fun map(item: PublicationStickersPack) = CardStickersPack(item)

    override fun reload() {
        super.reload()
    }

}

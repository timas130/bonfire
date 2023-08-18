package com.sayzen.campfiresdk.views

import android.view.View
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAll
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerMention
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.CardFandom
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

open class SplashSearchFandom(
        val onSelected: (ControllerMention.Field, Fandom) -> Unit
) : SplashSearch() {

    override fun instanceAdapter()
            = RecyclerCardAdapterLoading<CardFandom, Fandom>(CardFandom::class) {
        val card = CardFandom(it) { if(field != null)onSelected.invoke(field!!, it); hide() }
        card.setAvatarSize(ToolsView.dpToPx(32).toInt())
        card.setShowSubscribes(false)
        card
    }.setBottomLoader { onLoad, cards ->
        RFandomsGetAll(RFandomsGetAll.SUBSCRIBE_NONE, cards.size.toLong(), ControllerApi.getLanguageId(), 0, getSearchName(), emptyArray(), emptyArray(), emptyArray(), emptyArray())
                .onComplete { r ->
                    onLoad.invoke(r.fandoms)
                    vProgress.visibility = View.GONE
                }
                .onNetworkError { onLoad.invoke(null) }
                .send(api)
    }


}
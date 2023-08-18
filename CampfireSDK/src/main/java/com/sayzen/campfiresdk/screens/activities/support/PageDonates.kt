package com.sayzen.campfiresdk.screens.activities.support

import android.view.View
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.project.Donate
import com.dzen.campfire.api.requests.project.RProjectDonatesGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.cards.CardRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class PageDonates(
        val screen:SDonate
) : CardRecycler(){

    private val adapterX = RecyclerCardAdapterLoading<CardDonate, Donate>(CardDonate::class) { CardDonate(it) }

    init {
        vRecycler.adapter = adapterX

        vMessage.setText(t(API_TRANSLATE.app_loading))
        adapterX.setBottomLoader { onloaded, cards ->
            RProjectDonatesGetAll(cards.size.toLong())
                    .onComplete {
                        vMessage.setText(t(API_TRANSLATE.activities_support_empty_30))
                        if(it.donates.isNotEmpty()) vMessage.visibility = View.GONE
                        onloaded.invoke(it.donates)
                    }
                    .onError { onloaded.invoke(null) }
                    .send(api)
        }.setShowLoadingCard(false)
        adapterX.loadBottom()
    }

    override fun onReloadClicked() {
        adapterX.reloadBottom()
        screen.reload()
    }
}
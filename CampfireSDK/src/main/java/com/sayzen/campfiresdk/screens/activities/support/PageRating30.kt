package com.sayzen.campfiresdk.screens.activities.support

import android.view.View
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.project.Donate
import com.dzen.campfire.api.requests.project.RProjectDonatesRatingsGetAll30
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class PageRating30(
        val screen:SDonate
): CardRecycler(){

    private val adapterX = RecyclerCardAdapterLoading<CardDonate, Donate>(CardDonate::class) { CardDonate(it) }
    private var scrollToAccountId = 0L

    init {
        vRecycler.adapter = adapterX

        vMessage.setText(t(API_TRANSLATE.app_loading))
        adapterX.setBottomLoader { onLoaded, cards ->
            RProjectDonatesRatingsGetAll30(cards.size.toLong())
                    .onComplete {
                        vMessage.setText(t(API_TRANSLATE.activities_support_empty_30))
                        if(it.donates.isNotEmpty()) vMessage.visibility = View.GONE
                        onLoaded.invoke(it.donates)
                        scrollTo()
                    }
                    .onError { onLoaded.invoke(null) }
                    .send(api)
        }.setShowLoadingCard(false)
        adapterX.loadBottom()
    }

    fun scrollTo(accountId:Long){
        this.scrollToAccountId = accountId
        scrollTo()
    }

    private fun scrollTo(){
        if(scrollToAccountId < 1) return
        for(c in adapterX.get(CardDonate::class)) if(c.donate.account.id == scrollToAccountId){
            scrollToAccountId = 0
            ToolsView.scrollRecyclerSmooth(vRecycler, adapterX.indexOf(c))
            c.flash()
            break
        }
    }

    override fun onReloadClicked() {
        adapterX.reloadBottom()
        screen.reload()
    }
}
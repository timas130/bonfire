package com.sayzen.campfiresdk.screens.activities.support

import android.view.View
import android.view.inputmethod.EditorInfo
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.project.Donate
import com.dzen.campfire.api.requests.project.RProjectDonatesDraftsGetAll
import com.dzen.campfire.api.requests.project.RProjectSupportAdd
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.CardRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.java.tools.ToolsText

class PageDrafts(
        val screen:SDonate
) : CardRecycler(){

    private val adapterX = RecyclerCardAdapterLoading<CardDonate, Donate>(CardDonate::class) {
        CardDonate(it, false){ donate->
            ApiRequestsSupporter.executeEnabledConfirm("Записать донат от ${donate.account.name} на сумму ${ToolsText.numToStringRoundAndTrim(donate.sum / 100.0, 2)} \u20BD?", "Записать", RProjectSupportAdd(donate.account.id, donate.sum)) {
                ToolsToast.show(t(API_TRANSLATE.app_done))
                onReloadClicked()
            }
        }
    }

    init {
        (vFab as View).visibility = if (ControllerApi.account.getId() == 1L) View.VISIBLE else View.GONE
        vFab.setOnClickListener { addDonate() }
        vFab.setImageResource(R.drawable.ic_add_white_24dp)

        vRecycler.adapter = adapterX

        adapterX.setBottomLoader { onloaded, cards ->
            RProjectDonatesDraftsGetAll(cards.size.toLong())
                    .onComplete { onloaded.invoke(it.donates) }
                    .onError { onloaded.invoke(null) }
                    .send(api)
        }.setShowLoadingCard(false)
        adapterX.loadBottom()
    }

    override fun onReloadClicked() {
        screen.reload()
        adapterX.reloadBottom()
    }

    private fun addDonate() {
        Navigator.to(SAccountSearch(true, true) {
            val accountId = it.id
            SplashField()
                    .setHint("Сумма")
                    .setOnCancel(t(API_TRANSLATE.app_cancel))
                    .setInputType(EditorInfo.TYPE_NUMBER_FLAG_SIGNED)
                    .setOnEnter(t(API_TRANSLATE.app_add)) { w, sum ->
                        ApiRequestsSupporter.executeEnabled(w, RProjectSupportAdd(accountId, (sum.toDouble() * 100).toLong())) {
                            ToolsToast.show(t(API_TRANSLATE.app_done))
                            w.hide()
                            onReloadClicked()
                        }
                    }
                    .asSheetShow()
        })
    }
}
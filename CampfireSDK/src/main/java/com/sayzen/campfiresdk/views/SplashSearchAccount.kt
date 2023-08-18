package com.sayzen.campfiresdk.views

import android.view.View
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.accounts.RAccountsGetAll
import com.sayzen.campfiresdk.controllers.ControllerMention
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

open class SplashSearchAccount(
        val onSelected: (ControllerMention.Field, Account) -> Unit
) : SplashSearch() {

     override fun instanceAdapter() =
            RecyclerCardAdapterLoading<CardAccount, Account>(CardAccount::class) {
                val card = CardAccount(it)
                card.setOnClick { if(field != null) onSelected.invoke(field!!, it); hide() }
                card.setAvatarSize(ToolsView.dpToPx(32).toInt())
                card.setShowLvl(false)
                card
            }.setBottomLoader { onLoad, cards ->
                RAccountsGetAll()
                        .setUsername(getSearchName())
                        .setOffset(cards.size.toLong())
                        .onComplete { r ->
                            onLoad.invoke(r.accounts)
                            vProgress.visibility = View.GONE
                        }
                        .onNetworkError { onLoad.invoke(null) }
                        .send(api)
            }

}
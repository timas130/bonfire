package com.sayzen.campfiresdk.screens.account.rates

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.Rate
import com.dzen.campfire.api.requests.accounts.RAccountsRatesGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.rates.CardRate
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler

class SRates(
        accountId: Long,
        accountName: String
) : SLoadingRecycler<CardRate, Rate>() {

    private val xAccount = XAccount().setId(accountId).setName(accountName).setOnChanged { update() }

    init {
        disableShadows()
        disableNavigation()
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_21)
        setTextEmpty(if (ControllerApi.isCurrentAccount(accountId)) t(API_TRANSLATE.profile_rates_empty) else t(API_TRANSLATE.profile_rates_empty_another))
        update()

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RAccountsRatesGetAll(xAccount.getId(), cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.rates) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardRate::class

    override fun map(item: Rate) = CardRate(item)

    private fun update() {
        setTitle(t(API_TRANSLATE.app_rates) + if (ControllerApi.isCurrentAccount(xAccount.getId())) "" else " " + xAccount.getName())

    }

}

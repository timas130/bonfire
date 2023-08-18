package com.sayzen.campfiresdk.screens.account.karma

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.fandoms.KarmaInFandom
import com.dzen.campfire.api.requests.accounts.RAccountsKarmaInFandomsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler

class ScreenAccountKarma(
        accountId: Long,
        accountName: String
) : SLoadingRecycler<CardKarma, KarmaInFandom>() {

    private val xAccount = XAccount()
            .setId(accountId)
            .setName(accountName)
            .setOnChanged { update() }

    init {
        disableShadows()
        disableNavigation()
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_9)
        setTextEmpty(if (ControllerApi.isCurrentAccount(accountId)) t(API_TRANSLATE.profile_karma_empty) else t(API_TRANSLATE.profile_karma_empty_another))
        update()

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RAccountsKarmaInFandomsGetAll(xAccount.getId(), cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.karma) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardKarma::class

    override fun map(item: KarmaInFandom) = CardKarma(item)

    private fun update() {
        setTitle(t(API_TRANSLATE.app_karma) + if (ControllerApi.isCurrentAccount(xAccount.getId())) "" else " " + xAccount.getName())

    }

}

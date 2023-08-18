package com.sayzen.campfiresdk.screens.account.followers

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.accounts.RAccountsFollowsGetAll
import com.dzen.campfire.api.models.account.Account
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler

class SFollowers(
        accountId: Long,
        accountName: String
) : SLoadingRecycler<CardAccount, Account>() {

    private val xAccount = XAccount()
            .setId(accountId)
            .setName(accountName)
            .setOnChanged { update() }

    init {
        disableShadows()
        disableNavigation()
        setTextEmpty(if (ControllerApi.isCurrentAccount(accountId)) t(API_TRANSLATE.profile_followers_empty) else t(API_TRANSLATE.profile_followers_empty_another))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_24)
        update()

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RAccountsFollowsGetAll(xAccount.getId(), cards.size.toLong(), true)
                    .onComplete { r -> onLoad.invoke(r.accounts) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardAccount::class

    override fun map(item: Account) = CardAccount(item)

    private fun update() {
        setTitle(t(API_TRANSLATE.app_followers) + if (ControllerApi.isCurrentAccount(xAccount.getId())) "" else " " + xAccount.getName())
    }


}

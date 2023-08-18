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
import com.sayzen.campfiresdk.models.events.account.EventAccountsFollowsChange
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus

class SFollows(
        accountId: Long,
        accountName: String
) : SLoadingRecycler<CardAccount, Account>() {

    private val eventBus = EventBus.subscribe(EventAccountsFollowsChange::class) {
        if (!it.isFollow)
            for (c in adapter.get(CardAccount::class)) if (c.xAccount.getId() == it.accountId) adapter.remove(c)
    }

    private val xAccount = XAccount()
            .setId(accountId)
            .setName(accountName)
            .setOnChanged{ update() }

    init {
        disableShadows()
        disableNavigation()
        setTextEmpty(if (ControllerApi.isCurrentAccount(xAccount.getId())) t(API_TRANSLATE.profile_follows_empty) else t(API_TRANSLATE.profile_follows_empty_another))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_24)
        update()

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RAccountsFollowsGetAll(xAccount.getId(), cards.size.toLong(), false)
                    .onComplete { r -> onLoad.invoke(r.accounts) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardAccount::class

    override fun map(item: Account) = CardAccount(item)

    private fun update() {
        setTitle(t(API_TRANSLATE.app_follows) + if (ControllerApi.isCurrentAccount(xAccount.getId())) "" else " " + xAccount.getName())
    }

}

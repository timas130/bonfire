package com.sayzen.campfiresdk.screens.account.black_list

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.account.EventAccountAddToBlackList
import com.sayzen.campfiresdk.models.events.account.EventAccountRemoveFromBlackList
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus

class SBlackListUsers(
        val accountId: Long,
        val accountName: String
) : SLoadingRecycler<CardAccount, Account>() {

    val eventBud = EventBus
            .subscribe(EventAccountAddToBlackList::class) { reload() }
            .subscribe(EventAccountRemoveFromBlackList::class) { onEventAccountRemoveFromBlackList(it) }

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.settings_black_list_users))
        setTextEmpty(t(API_TRANSLATE.settings_black_list_empty))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_22)

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RAccountsBlackListGetAll(accountId, cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.accounts) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardAccount::class

    override fun map(item: Account): CardAccount {
        val c = CardAccount(item)
        c.setDividerVisible(false)
        if (accountId == ControllerApi.account.getId()) c.setOnClick { ControllerCampfireSDK.removeFromBlackListUser(item.id) }
        return c
    }

    //
    //  EventBus
    //

    private fun onEventAccountRemoveFromBlackList(e: EventAccountRemoveFromBlackList) {
        val cards = adapter.get(CardAccount::class)
        for (c in cards) if (c.xAccount.getId() == e.accountId) adapter.remove(c)
    }

}

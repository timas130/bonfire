package com.sayzen.campfiresdk.screens.account

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.accounts.RAccountsGetAllOnline
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.views.screens.SLoadingRecycler

class SAccountsOnline : SLoadingRecycler<CardAccount, Account>() {

    init {
        disableShadows()
        disableNavigation()

        setTitle(t(API_TRANSLATE.app_online))
        setTextEmpty(t(API_TRANSLATE.app_empty))
        setBackgroundImage(ImageLoader.load(ApiResources.IMAGE_BACKGROUND_4))

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RAccountsGetAllOnline(cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.accounts) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardAccount::class

    override fun map(item: Account) = CardAccount(item)

}

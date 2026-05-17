package com.sayzen.campfiresdk.screens.fandoms

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.requests.fandoms.RFandomsSubscribersGetAll
import com.dzen.campfire.api.models.account.Account
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.views.screens.SLoadingRecycler

class SSubscribers(
    private val fandomId: Long,
    private val languageId: Long,
) : SLoadingRecycler<CardAccount, Account>() {
    init {
        disableShadows()
        disableNavigation()
        setTextEmpty(t(API_TRANSLATE.fandom_subscribers_empty))
        setBackgroundImage(ImageLoader.load(ApiResources.IMAGE_BACKGROUND_24))
        setTitle(t(API_TRANSLATE.app_subscribers))

        adapter.setBottomLoader { onLoad, cards ->
            RFandomsSubscribersGetAll(cards.size.toLong(), fandomId, languageId)
                .onComplete { r -> onLoad.invoke(r.accounts) }
                .onNetworkError { onLoad.invoke(null) }
                .send(api)
        }
    }

    override fun classOfCard() = CardAccount::class

    override fun map(item: Account) = CardAccount(item)
}

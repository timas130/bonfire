package com.sayzen.campfiresdk.screens.account.fandoms

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAllSubscribed
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardFandom
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import kotlin.reflect.KClass

class SAcounFandoms(
        val accountId: Long
) : SLoadingRecycler<CardFandom, Fandom>() {

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_fandoms))
        setTextEmpty(t(API_TRANSLATE.fandoms_empty))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_4)

        adapter.setBottomLoader { onLoad, cards ->
            RFandomsGetAllSubscribed(accountId, cards.size.toLong())
                    .onComplete { r ->
                        onLoad.invoke(r.fandoms)
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardFandom::class

    override fun map(item: Fandom) =  CardFandom(item)

}

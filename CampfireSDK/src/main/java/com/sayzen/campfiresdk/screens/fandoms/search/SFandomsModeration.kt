package com.sayzen.campfiresdk.screens.fandoms.search


import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAllModerated
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.CardFandom
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import kotlin.reflect.KClass

class SFandomsModeration(
        private val accountId: Long
) : SLoadingRecycler<CardFandom, Fandom>() {


    companion object {

        fun instance(accountId: Long, action: NavigationAction) {
            Navigator.action(action, SFandomsModeration(accountId))
        }
    }

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_fandoms))
        setTextEmpty(t(API_TRANSLATE.fandoms_empty))

        adapter.setBottomLoader { onLoad, cards ->
            RFandomsGetAllModerated(accountId, cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.fandoms) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)

        }
    }

    override fun classOfCard() = CardFandom::class

    override fun map(item: Fandom) = CardFandom(item)

}

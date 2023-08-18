package com.sayzen.campfiresdk.screens.activities.user_activities.relay_race

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.activities.RActivitiesRelayGetMembers
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import kotlin.reflect.KClass

class SRelayRaceInfoMembers(
        val userActivityId: Long
) : SLoadingRecycler<CardAccount, Account>() {

    init {
        disableNavigation()
        disableShadows()
        setTitle(t(API_TRANSLATE.activities_relay_race_members))
        setTextEmpty(t(API_TRANSLATE.app_empty))

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RActivitiesRelayGetMembers(userActivityId, cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.accounts) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardAccount::class

    override fun map(item: Account) = CardAccount(item)

}

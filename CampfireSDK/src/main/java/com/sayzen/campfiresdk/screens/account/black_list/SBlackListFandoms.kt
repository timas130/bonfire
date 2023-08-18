package com.sayzen.campfiresdk.screens.account.black_list

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.accounts.RAccountsGetIgnoredFandoms
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAllById
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.models.cards.CardFandom
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBlackListChange
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus

class SBlackListFandoms(
        val accountId: Long,
        val accountName: String,
        val feedIgnoreFandoms: Array<Long>
) : SLoadingRecycler<CardFandom, Fandom>() {

    companion object {

        fun instance(accountId: Long, accountName: String, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RAccountsGetIgnoredFandoms(accountId)) { r -> SBlackListFandoms(accountId, accountName, r.fandomsIds) }
        }

    }

    val eventBud = EventBus
            .subscribe(EventFandomBlackListChange::class) {
                if (!it.inBlackList)
                    for (c in adapter.get(CardFandom::class)) if (c.fandom.id == it.fandomId) adapter.remove(c)
            }

    var loaded = false

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.settings_black_list_fandoms))
        setTextEmpty(t(API_TRANSLATE.settings_black_list_empty))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_22)

        adapter.setBottomLoader { onLoad, _ ->
            if (loaded) {
                onLoad.invoke(emptyArray())
                return@setBottomLoader
            }

            subscription = RFandomsGetAllById(feedIgnoreFandoms)
                    .onComplete { r ->
                        loaded = true
                        onLoad.invoke(r.fandoms)
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardFandom::class

    override fun map(item: Fandom): CardFandom {
        val c = CardFandom(item)
        if (accountId == ControllerApi.account.getId()) c.onClick = { ControllerCampfireSDK.removeFromBlackListFandom(item.id) }
        c.setSubscribed(false)
        return c
    }

    override fun reload() {
        loaded = false
        super.reload()
    }

}

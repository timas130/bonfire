package com.sayzen.campfiresdk.screens.punishments

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.AccountPunishment
import com.dzen.campfire.api.requests.accounts.RAccountsPunishmentsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler

class SPunishments(
        accountId: Long,
        accountName: String
) : SLoadingRecycler<CardPunishment, AccountPunishment>() {

    private val xAccount = XAccount()
            .setId(accountId)
            .setName(accountName)
            .setOnChanged{ update() }

    init {
        disableShadows()
        disableNavigation()

        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_8)
        setTextEmpty(if (ControllerApi.isCurrentAccount(accountId)) t(API_TRANSLATE.profile_punishments_empty) else t(API_TRANSLATE.profile_punishments_empty_another))
        update()

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RAccountsPunishmentsGetAll(xAccount.getId(), cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.punishments) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardPunishment::class

    override fun map(item: AccountPunishment) = CardPunishment(item)

    private fun update(){
        setTitle(t(API_TRANSLATE.app_punishments) + if (ControllerApi.isCurrentAccount(xAccount.getId())) "" else " " + xAccount.getName())
    }

}

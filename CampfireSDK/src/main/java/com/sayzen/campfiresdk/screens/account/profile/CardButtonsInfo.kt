package com.sayzen.campfiresdk.screens.account.profile

import com.dzen.campfire.api.requests.accounts.RAccountsGetProfile
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.account.EventAccountPunishmentRemove
import com.sayzen.campfiresdk.models.events.account.EventAccountsFollowsChange
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.libs.eventBus.EventBus

abstract class CardButtonsInfo(
        private val xAccount: XAccount,
        layout: Int
) : Card(layout) {
    val eventBus = EventBus
            .subscribe(EventAccountsFollowsChange::class) { this.onAccountsFollowChange(it) }
            .subscribe(EventAccountPunishmentRemove::class) { this.onEventAccountPunishmentRemove(it) }

    var profile: RAccountsGetProfile.Response? = null

    abstract fun updateFollowersCount()

    abstract fun updatePunishments()

    abstract fun updateKarma()

    fun setInfo(profile: RAccountsGetProfile.Response) {
        this.profile = profile
        update()
    }

    private fun onAccountsFollowChange(e: EventAccountsFollowsChange) {
        if (ControllerApi.isCurrentAccount(xAccount.getId())) {
            if (profile != null) {
                profile!!.followsCount += if (e.isFollow) 1 else -1
            }
            updateFollowersCount()
        }
    }

    private fun onEventAccountPunishmentRemove(e: EventAccountPunishmentRemove) {
        if (xAccount.getId() == e.accountId) {
            if (e.isWarn && profile != null) {
                profile!!.warnsCount--
            }
            if (e.isBan && profile != null) {
                profile!!.bansCount--
            }
            updatePunishments()
        }
    }
}

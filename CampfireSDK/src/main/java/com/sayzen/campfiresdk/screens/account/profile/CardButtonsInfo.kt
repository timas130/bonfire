package com.sayzen.campfiresdk.screens.account.profile

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

    var followsCount: Long? = null
    var followersCount: Long? = null
    var warnsCount: Long? = null
    var bansCountCount: Long? = null
    var karmaTotal: Long? = null
    var rates: Long? = null
    var moderationFandomsCount: Long? = null
    var subscribedFandomsCount: Long? = null
    var stickersCount: Long? = null
    var blackAccountsCount: Long? = null
    var blackFandomsCount: Long? = null
    var loaded = false

    abstract fun updateFollowersCount()

    abstract fun updatebansPunishments()

    abstract fun updatebansKarma()

    fun setInfo(followsCount: Long, followersCount: Long, warnsCount: Long, bansCountCount: Long, karmaTotal: Long, rates: Long,
                moderationFandomsCount: Long, subscribedFandomsCount: Long, stickersCount: Long, blackAccountsCount: Long, blackFandomsCount: Long) {
        this.loaded = true
        this.followsCount = followsCount
        this.followersCount = followersCount
        this.warnsCount = warnsCount
        this.bansCountCount = bansCountCount
        this.karmaTotal = karmaTotal
        this.rates = rates
        this.moderationFandomsCount = moderationFandomsCount
        this.subscribedFandomsCount = subscribedFandomsCount
        this.stickersCount = stickersCount
        this.blackAccountsCount = blackAccountsCount
        this.blackFandomsCount = blackFandomsCount
        updateFollowersCount()
        updatebansPunishments()
        updatebansKarma()
    }

    private fun onAccountsFollowChange(e: EventAccountsFollowsChange) {
        if (ControllerApi.isCurrentAccount(xAccount.getId())) {
            if (followsCount == null) followsCount = (if (e.isFollow) 1 else -1).toLong()
            else followsCount = followsCount!! + (if (e.isFollow) 1 else -1).toLong()
            updateFollowersCount()
        }
    }

    private fun onEventAccountPunishmentRemove(e: EventAccountPunishmentRemove) {
        if (xAccount.getId() == e.accountId) {
            if(e.isWarn){
                if(warnsCount != null && warnsCount!!>0) warnsCount = warnsCount!!-1
            }
            if(e.isBan){
                if(bansCountCount != null && bansCountCount!!>0) bansCountCount = bansCountCount!!-1
            }
            updatebansPunishments()
        }
    }

}

package com.sayzen.campfiresdk.compose.data

import com.dzen.campfire.api.models.account.Account
import com.sayzen.campfiresdk.compose.BonfireDataSource
import com.sayzen.campfiresdk.models.events.account.EventAccountChanged
import com.sayzen.campfiresdk.models.events.account.EventAccountEffectAdd
import com.sayzen.campfiresdk.models.events.account.EventAccountEffectRemove
import com.sayzen.campfiresdk.models.events.account.EventAccountOnlineChanged
import com.sup.dev.java.tools.ToolsCollections

class AccountDataSource(data: Account) : BonfireDataSource<Account>(data) {
    init {
        subscriber
            .subscribe(EventAccountChanged::class) {
                edit(it.accountId) {
                    if (it.name.isNotEmpty()) name = it.name
                    if (it.image.isNotEmpty()) image = it.image
                }
            }
            .subscribe(EventAccountOnlineChanged::class) {
                edit(it.accountId) {
                    lastOnlineDate = it.onlineTime
                }
            }
            .subscribe(EventAccountEffectAdd::class) {
                edit(it.accountId) {
                    accountEffects += it.mEffect
                }
            }
            .subscribe(EventAccountEffectRemove::class) { ev ->
                edit(ev.accountId) {
                    accountEffects = ToolsCollections.removeIf(accountEffects) { it.id == ev.effectId }
                }
            }
    }

    private fun edit(id: Long, editor: Account.() -> Unit) {
        edit(id == data.id, editor)
    }
}

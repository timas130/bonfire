package com.sayzen.campfiresdk.models.cards

import com.dzen.campfire.api.models.account.Account
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle

open class CardAccount(
        val account: Account,
        layout: Int = 0
) : CardAvatar(layout), NotifyItem {

    val xAccount = XAccount().setAccount(account).setOnChanged { update() }

    init {
        setTitle(account.name)
        setOnClick { xAccount.toProfileScreen() }
        setDividerVisible(true)
    }

    override fun onBind(vAvatar: ViewAvatarTitle) {
        xAccount.setView(vAvatar)
    }

    override fun notifyItem() {
        ImageLoader.load(xAccount.getImageId()).intoCash()
    }

    override fun setOnClick(onClick: () -> Unit): CardAccount {
        return super.setOnClick(onClick) as CardAccount
    }

    fun setShowLvl(showLvl: Boolean) {
        xAccount.showLevel = showLvl
        update()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is CardAccount) account.id == other.account.id
        else super.equals(other)
    }

}

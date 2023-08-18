package com.sayzen.campfiresdk.screens.account.karma

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.fandoms.KarmaInFandom
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewAvatarTitle

class CardKarma(
        val karma: KarmaInFandom
) : CardAvatar(R.layout.screen_account_karma_card), NotifyItem {

    private val xFandom = XFandom().setFandom(karma.fandom).setOnChanged{update()}

    init {
        setOnClick { SFandom.instance(karma.fandom, Navigator.TO) }
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val vRate: TextView = view.findViewById(R.id.vRate)

        vRate.text = (karma.karmaCount / 100).toString()
        vRate.setTextColor(ToolsResources.getColor(if (karma.karmaCount > 0L) R.color.green_700 else R.color.red_700))
    }

    override fun onBind(vAvatar: ViewAvatarTitle) {
        xFandom.setView(vAvatar)
        ControllerLinks.makeLinkable(vAvatar.vTitle)
        ImageLoader.load(karma.fandom.imageId).into(vAvatar.vAvatar.vImageView)
    }

    override fun notifyItem() {
        ImageLoader.load(karma.fandom.imageId).intoCash()
    }
}
package com.sayzen.campfiresdk.screens.rates

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.Rate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText

class CardRateText(
        val rate: Rate
) : CardAvatar(R.layout.card_rate_text), NotifyItem {

    private val xAccount = XAccount().setAccount(rate.account).setOnChanged { update() }

    init {
        if(rate.account.id < 1) xAccount.setImageId(API_RESOURCES.CAMPFIRE_IMAGE_4)
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val vRate: TextView = view.findViewById(R.id.vRate)
        val vCof: TextView = view.findViewById(R.id.vCof)

        vCof.text = "(x${ToolsText.numToStringRoundAndTrim(rate.karmaCof / 100.0, 2)})"
        if (rate.karmaCof == 100L || rate.karmaCof == 0L) {
            vCof.visibility = View.GONE
        } else {
            vCof.visibility = View.VISIBLE
        }

        vRate.text = (rate.karmaCount / 100).toString()
        vRate.setTextColor(ToolsResources.getColor(if (rate.karmaCount > 0L) R.color.green_700 else R.color.red_700))
    }

    override fun onBind(vAvatar: ViewAvatarTitle) {

        xAccount.setView(vAvatar)
        vAvatar.setSubtitle(ToolsDate.dateToString(rate.date))

        if (xAccount.getId() == 0L) {
            vAvatar.setTitle(t(API_TRANSLATE.rate_anon))
        }
    }

    override fun notifyItem() {
        ImageLoader.load(xAccount.getImageId()).intoCash()
    }
}

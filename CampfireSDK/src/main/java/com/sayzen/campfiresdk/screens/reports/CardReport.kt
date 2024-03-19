package com.sayzen.campfiresdk.screens.reports

import android.view.View
import com.dzen.campfire.api.models.publications.PublicationReport
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewAvatarTitle

class CardReport(
        val report: PublicationReport
) : Card(R.layout.screen_reports_card), NotifyItem {

    val xAccount = XAccount().setAccount(report.account)

    override fun bindView(view: View) {
        super.bindView(view)

        val vAvatar:ViewAvatarTitle = view.findViewById(R.id.vAvatar)

        xAccount.setView(vAvatar)
        vAvatar.setSubtitle(report.comment)
    }

    override fun notifyItem() {
        ImageLoader.load(xAccount.getImage()).intoCash()
    }
}

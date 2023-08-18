package com.sayzen.campfiresdk.screens.reports

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationReport
import com.dzen.campfire.api.models.publications.Rate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.tools.ToolsDate

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
        ImageLoader.load(xAccount.getImageId()).intoCash()
    }
}
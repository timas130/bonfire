package com.sayzen.campfiresdk.screens.wiki.history

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.wiki.WikiPages
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tPlural
import com.sayzen.campfiresdk.screens.wiki.SWikiArticleView
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus

class CardHistory(
        val screen:SWikiArticleHistory,
        val pages: WikiPages,
        val wikiTitle: WikiTitle,
        val languageId: Long
) : Card(R.layout.screen_wiki_article_history_card) {

    private val eventBus = EventBus
            .subscribe(EventWikiHistoryStatusChanged::class) {
                if (it.pagesId == pages.id) {
                    pages.wikiStatus = it.newStatus
                    if(it.newStatus == API.STATUS_PUBLIC) screen.updateStatus()
                    update()
                }
            }

    val xAccount = XAccount()
            .setId(pages.creatorId)
            .setName(pages.creatorName)
            .setImageId(pages.creatorImageId)
            .setDate(pages.changeDate)

    override fun bindView(view: View) {
        super.bindView(view)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)

        xAccount.setView(vAvatar)
        vAvatar.makeSoftware()
        vAvatar.setOnClickListener { Navigator.to(SWikiArticleView(wikiTitle, languageId, pages)) }

        val label = when (pages.wikiStatus) {
            API.STATUS_PUBLIC -> "{green ${t(API_TRANSLATE.app_actual)}}"
            API.STATUS_REMOVED -> "{red ${t(API_TRANSLATE.app_removed)}}"
            else -> "{grey ${t(API_TRANSLATE.app_archive)}}"
        }

        vAvatar.vSubtitle.text = "${vAvatar.vSubtitle.text} ${pages.pages.size} ${tPlural(pages.pages.size, API_TRANSLATE.pages_count)}\n$label"
        ControllerLinks.makeLinkable(vAvatar.vSubtitle)

    }

}
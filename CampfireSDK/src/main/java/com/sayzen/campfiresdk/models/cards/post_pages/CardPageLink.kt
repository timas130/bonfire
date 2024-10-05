package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PagesContainer

import com.dzen.campfire.api.models.publications.post.PageLink
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerExternalLinks
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewText
import sh.sit.bonfire.formatting.BonfireMarkdown

class CardPageLink(
    pagesContainer: PagesContainer?,
    page: PageLink
) : CardPage(R.layout.card_page_link, pagesContainer, page) {
    override fun bindView(view: View) {
        super.bindView(view)

        val vName: ViewText = view.findViewById(R.id.vName)
        val vLink: ViewText = view.findViewById(R.id.vLink)
        val vTouch: View = view.findViewById(R.id.vTouch)
        val page = page as PageLink

        vTouch.visibility = if (clickable) View.VISIBLE else View.GONE
        vTouch.setOnClickListener { ControllerExternalLinks.openLink(page.link) }
        vTouch.setOnLongClickListener {
            ToolsAndroid.setToClipboard(page.link)
            ToolsToast.show(t(API_TRANSLATE.app_copied))
            true
        }

        BonfireMarkdown.setMarkdownInline(vName, page.name)

        // fixme: this is plain evil
        vLink.text = page.link
        ControllerLinks.makeLinkable(vLink)
    }

    override fun notifyItem() {}
}

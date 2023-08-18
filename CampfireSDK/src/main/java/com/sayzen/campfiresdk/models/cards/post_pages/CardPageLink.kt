package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PagesContainer

import com.dzen.campfire.api.models.publications.post.PageLink
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewText

class CardPageLink(
        pagesContainer: PagesContainer?,
        page: PageLink
) : CardPage(R.layout.card_page_link, pagesContainer, page) {

    override fun bindView(view: View) {
        super.bindView(view)


        val vName: ViewText = view.findViewById(R.id.vName)
        val vLink: ViewText = view.findViewById(R.id.vLink)
        val vTouch: View = view.findViewById(R.id.vTouch)

        ControllerLinks.makeLinkable(vName)

        vTouch.visibility = if (clickable) View.VISIBLE else View.GONE
        vTouch.setOnClickListener { ControllerLinks.openLink((page as PageLink).link) }
        vTouch.setOnLongClickListener {
            ToolsAndroid.setToClipboard((page as PageLink).link)
            ToolsToast.show(t(API_TRANSLATE.app_copied))
            true
        }

        vLink.text = (page as PageLink).link
        vName.text = (page as PageLink).name

        ControllerLinks.makeLinkable(vLink)
    }


    override fun notifyItem() {}
}
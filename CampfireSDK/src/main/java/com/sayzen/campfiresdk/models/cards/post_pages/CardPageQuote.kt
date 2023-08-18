package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.models.publications.PagesContainer

import com.dzen.campfire.api.models.publications.post.PageQuote
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sup.dev.android.views.views.ViewText

class CardPageQuote(
        pagesContainer: PagesContainer?,
        page: PageQuote
) : CardPage(R.layout.card_page_quote, pagesContainer, page) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vText: ViewText = view.findViewById(R.id.vText)
        val vAuthor: ViewText = view.findViewById(R.id.vAuthor)

        vAuthor.visibility = if ((page as PageQuote).author.isEmpty()) View.GONE else View.VISIBLE

        vText.setTextIsSelectable(clickable)
        vAuthor.setTextIsSelectable(clickable)

        vText.text = (page as PageQuote).text
        vAuthor.text = (page as PageQuote).author + ":"

        ControllerLinks.makeLinkable(vText)
        ControllerLinks.makeLinkable(vAuthor)

    }

    override fun notifyItem() {}
}

package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PageQuote
import com.sayzen.campfiresdk.R
import com.sup.dev.android.views.views.ViewText
import sh.sit.bonfire.formatting.BonfireMarkdown

class CardPageQuote(
        pagesContainer: PagesContainer?,
        page: PageQuote
) : CardPage(R.layout.card_page_quote, pagesContainer, page) {
    override fun bindView(view: View) {
        super.bindView(view)
        val vText: ViewText = view.findViewById(R.id.vText)
        val vAuthor: ViewText = view.findViewById(R.id.vAuthor)

        val page = page as PageQuote

        vAuthor.visibility = if (page.author.isEmpty()) View.GONE else View.VISIBLE

        vText.setTextIsSelectable(clickable)
        vAuthor.setTextIsSelectable(clickable)

        BonfireMarkdown.setMarkdownInline(vAuthor, page.author + ":")
        BonfireMarkdown.setMarkdown(vText, page.text)
    }

    override fun notifyItem() {}
}

package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PagePolling
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XPolling

class CardPagePolling(
        pagesContainer: PagesContainer?,
        page: PagePolling
) : CardPage(R.layout.card_page_polling, pagesContainer, page) {

    private var xPolling = XPolling(page, pagesContainer, {editMode}, {postIsDraft}) { update() }

    override fun bindView(view: View) {
        super.bindView(view)
        if(xPolling.page != page) xPolling = XPolling(page as PagePolling, pagesContainer, {editMode}, {postIsDraft}) { update() }
        xPolling.setView(view)
    }

    override fun notifyItem() {

    }
}
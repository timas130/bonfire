package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.Page

import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t

class CardPageUnknown(
        pagesContainer: PagesContainer?,
        page: Page
) : CardPage(R.layout.card_page_unknown, pagesContainer, page) {


    override fun bindView(view: View) {
        super.bindView(view)
        val vSystemMessage:TextView = view.findViewById(R.id.vSystemMessage)
        vSystemMessage.text = t(API_TRANSLATE.error_unknown)
    }

    override fun notifyItem() {}
}

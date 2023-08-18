package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PagesContainer

import com.dzen.campfire.api.models.publications.post.PageSpoiler
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sayzen.campfiresdk.controllers.tPlural
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.views.ViewText
import java.util.ArrayList

class CardPageSpoiler(
        pagesContainer: PagesContainer?,
        page: PageSpoiler
) : CardPage(R.layout.card_page_spoiler, pagesContainer, page) {

    var pages:ArrayList<CardPage>? = null
    var onClick:()->Unit = {}

    override fun bindView(view: View) {
        super.bindView(view)
        val vText: ViewText = view.findViewById(R.id.vText)
        val vIcon:ImageView = view.findViewById(R.id.vIcon)
        val vTouch:View = view.findViewById(R.id.vTouch)

        vTouch.visibility = if (clickable) View.VISIBLE else View.GONE
        vTouch.setOnClickListener { onClicked() }

        vIcon.setImageResource(if ((page as PageSpoiler).isOpen) R.drawable.ic_keyboard_arrow_up_white_24dp else R.drawable.ic_keyboard_arrow_down_white_24dp)
        vText.text = (page as PageSpoiler).name + " (" + (page as PageSpoiler).count + " " + tPlural((page as PageSpoiler).count, API_TRANSLATE.pages_count) + ")"

        ControllerLinks.makeLinkable(vText)

    }


    private fun onClicked() {
        (page as PageSpoiler).isOpen = !(page as PageSpoiler).isOpen
        update()
        if (adapter != null && adapter is RecyclerCardAdapter) ControllerPost.updateSpoilers((adapter as RecyclerCardAdapter?)!!)
        if (pages != null) ControllerPost.updateSpoilers(pages!!)
        onClick.invoke()
    }

    override fun notifyItem() {}


}
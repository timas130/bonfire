package com.sayzen.campfiresdk.screens.other.gallery

import android.view.View
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewText

class CardTitle(
        val title:String,
        val text:String
) : Card(R.layout.screen_other_gallery_card_title){

    override fun bindView(view: View) {
        super.bindView(view)

        val vTitle:ViewText = view.findViewById(R.id.vTitle)
        val vText:ViewText = view.findViewById(R.id.vText)

        vTitle.text = title
        vText.text = text

        ControllerLinks.makeLinkable(vTitle)
        ControllerLinks.makeLinkable(vText)

    }

}
package com.sayzen.campfiresdk.screens.achievements

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.image_loader.ImageLoader

import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.tools.ToolsText

class CardInfo(
        var text: String,
        var countTitle: String,
        var count: Long,
        var dot: Boolean,
        var image: Long = 0L
) : Card(R.layout.screen_achievements_card_info) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vCount: TextView = view.findViewById(R.id.vCount)
        val vCountTitle: TextView = view.findViewById(R.id.vCountTitle)
        val vText: TextView = view.findViewById(R.id.vText)
        val vImage: ImageView = view.findViewById(R.id.vImage)

        vImage.visibility = if (image == 0L) View.GONE else View.VISIBLE
        if (image != 0L) ImageLoader.load(image).noHolder().into(vImage)

        vCountTitle.setText(countTitle)
        vText.setText(text)

        if (dot) vCount.text = ToolsText.numToStringRound(count / 100.0, 2)
        else vCount.text = "${count / 100}"
    }
}

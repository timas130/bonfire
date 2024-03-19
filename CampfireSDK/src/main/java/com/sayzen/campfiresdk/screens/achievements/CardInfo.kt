package com.sayzen.campfiresdk.screens.achievements

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.images.ImageRef
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader

import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.tools.ToolsText

class CardInfo(
        var text: String,
        var countTitle: String,
        var count: Long,
        var dot: Boolean,
        var image: ImageRef = ImageRef(),
) : Card(R.layout.screen_achievements_card_info) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vCount: TextView = view.findViewById(R.id.vCount)
        val vCountTitle: TextView = view.findViewById(R.id.vCountTitle)
        val vText: TextView = view.findViewById(R.id.vText)
        val vImage: ImageView = view.findViewById(R.id.vImage)

        vImage.visibility = if (image.isEmpty()) View.GONE else View.VISIBLE
        if (image.isNotEmpty()) ImageLoader.load(image).noHolder().into(vImage)

        vCountTitle.setText(countTitle)
        vText.setText(text)

        if (dot) vCount.text = ToolsText.numToStringRound(count / 100.0, 2)
        else vCount.text = "${count / 100}"
    }
}

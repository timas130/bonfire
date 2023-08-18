package com.sayzen.campfiresdk.screens.achievements.lvl

import android.view.View
import android.widget.TextView

import com.dzen.campfire.api.models.lvl.LvlInfoAdmin
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.objects.AppLevel
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.tools.ToolsText

class CardLvlTitle(
        val text:String,
        val color:Int
) : Card(R.layout.screen_achievements_card_lvl_title) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vText: TextView = view.findViewById(R.id.vText)

        vText.text = text
        vText.setTextColor(color)
    }

}

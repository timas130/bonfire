package com.sup.dev.android.views.cards

import android.view.View
import android.widget.TextView
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsView

class CardMenuButton : CardMenu(R.layout.card_menu_button) {

    override fun bindView(view: View) {
        super.bindView(view)

        val vText: TextView = view.findViewById(R.id.vText)

        ToolsView.setOnClickCoordinates(vText) { v, x, y -> onClick(v,x,y) }
    }

}

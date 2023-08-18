package com.sup.dev.android.views.cards

import android.view.View
import android.view.ViewGroup
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsView

class CardView(
        val viewForCard:View
) : Card(R.layout.card_view) {

    override fun bindView(view: View) {
        super.bindView(view)
        view as ViewGroup
        view.removeAllViews()
        view.addView(viewForCard)
    }

}

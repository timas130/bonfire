package com.sayzen.campfiresdk.screens.other.gallery

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.sayzen.campfiresdk.R
import com.sup.dev.android.views.cards.Card

class CardContainer(
        val screen: SGallery,
        val startIndex: Int,
        val array: Array<CardImage>,
        val count:Int
) : Card(R.layout.screen_other_gallery_card_container) {

    override fun bindView(view: View) {
        super.bindView(view)

        val vContainer: LinearLayout = view.findViewById(R.id.vContainer)
        vContainer.removeAllViews()

        for(i in 0 until count) add(vContainer, startIndex + i)
    }

    fun add(vContainer: ViewGroup, imageIndex: Int) {
        val vL = LinearLayout(vContainer.context)
        vContainer.addView(vL)
        (vL.layoutParams as LinearLayout.LayoutParams).weight = 1f
        (vL.layoutParams as LinearLayout.LayoutParams).width = ViewGroup.LayoutParams.MATCH_PARENT
        (vL.layoutParams as LinearLayout.LayoutParams).height = ViewGroup.LayoutParams.MATCH_PARENT

        if(array.size <= imageIndex) return
        val v = array[imageIndex].instanceView(vL)
        array[imageIndex].bindView(v)
        vL.addView(v)

    }


}
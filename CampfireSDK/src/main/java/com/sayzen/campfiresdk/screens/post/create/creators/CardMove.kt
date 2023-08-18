package com.sayzen.campfiresdk.screens.post.create.creators

import com.sup.dev.android.views.cards.Card

import android.view.View
import com.sayzen.campfiresdk.R

class CardMove(private val onSelected: ()->Unit) : Card(R.layout.screen_post_create_card_move) {

    override fun bindView(view: View) {
        super.bindView(view)
        view.setOnClickListener { onSelected.invoke() }
    }
}

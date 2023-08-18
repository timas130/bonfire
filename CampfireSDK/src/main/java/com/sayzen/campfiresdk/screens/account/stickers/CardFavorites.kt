package com.sayzen.campfiresdk.screens.account.stickers

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.Card

class CardFavorites(
        val accountId:Long
) : Card(R.layout.screen_stickers_favorites){

    var onClick:()->Unit = {Navigator.to(SStickersViewFavorite(accountId))}

    override fun bindView(view: View) {
        super.bindView(view)

        val vTitleAppFavor:TextView = view.findViewById(R.id.vTitleAppFavor)

        vTitleAppFavor.text = t(API_TRANSLATE.app_favorites)

        view.setOnClickListener {
            onClick.invoke()
        }
    }

}
package com.sayzen.campfiresdk.screens.account.rating


import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.dzen.campfire.api.models.account.Account
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatar

class CardRating(
        private val account: Account,
        private var text: String?
) : Card(R.layout.screen_rating_card_rating) {

    private var index = 0
    private var textColor = 0
    private var textColorDef: Int = 0
    private var defSeted: Boolean = false

    private val xAccount = XAccount().setAccount(account).setOnChanged { update() }

    override fun bindView(view: View) {
        super.bindView(view)
        val vText: TextView = view.findViewById(R.id.vText)
        val vIndex: TextView = view.findViewById(R.id.vIndex)
        val vAvatar: ViewAvatar = view.findViewById(R.id.vImage)
        val vName: TextView = view.findViewById(R.id.vName)
        val vImageBackground: ImageView = view.findViewById(R.id.vImageBackground)

        var background = 0
        if (ControllerApi.isCurrentAccount(xAccount.getId())) background = ToolsResources.getColor(R.color.focus)
        if (index == 1) background = ToolsResources.getColor(R.color.yellow_a_200)
        if (index == 2) background = ToolsResources.getColor(R.color.grey_300)
        if (index == 3) background = ToolsResources.getColor(R.color.brown_600)

        xAccount.setView(vAvatar)
        vName.text = account.name

        vIndex.text = "$index"

        if (!defSeted) {
            defSeted = true
            textColorDef = vText.currentTextColor
        }

        vImageBackground.visibility = if (background == 0) View.INVISIBLE else View.VISIBLE
        vImageBackground.setImageDrawable(ColorDrawable(background))
        vText.setTextColor(if (textColor != 0) textColor else textColorDef)

        vText.text = text

        view.setOnClickListener { SProfile.instance(account, Navigator.TO)}

    }

    //
    //  Setters
    //

    fun setIndex(index: Int): CardRating {
        this.index = index
        update()
        return this
    }

    fun setText(text: String): CardRating {
        this.text = text
        update()
        return this
    }

    fun setTextColor(color: Int): CardRating {
        textColor = color
        update()
        return this
    }

}

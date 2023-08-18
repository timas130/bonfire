package com.sayzen.campfiresdk.models.cards

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import android.view.View
import android.widget.TextView
import com.sayzen.campfiresdk.R

import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.ViewIcon

class CardCategory private constructor(
    private val title: String,
    @param:DrawableRes private val imageResId: Int,
    @param:DrawableRes private val iconId: Int,
    private val iconBackground: Int?,
    private val onClick:  ()->Unit) : Card(R.layout.card_category) {

    private var chipText: String? = null

    constructor(title: String, @DrawableRes imageResId: Int, onClick: ()->Unit) : this(title, imageResId, 0, null, onClick) {}

    fun setChip(chipText: String): CardCategory {
        this.chipText = chipText
        update()
        return this
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vImage = view.findViewById<ViewAvatar>(R.id.vImage)
        val vTitle = view.findViewById<TextView>(R.id.vTitle)
        val vTouch = view.findViewById<View>(R.id.vTouch)
        val vIcon = view.findViewById<ViewIcon>(R.id.vIcon)
        val vChip:ViewChip = view.findViewById(R.id.vChip)

        vTitle.text = title
        vChip.text = chipText
        if (iconId != 0) {
            vIcon.setImageResource(iconId)
            vImage.visibility = View.GONE
            vIcon.visibility = View.VISIBLE
            if (iconBackground != null) vIcon.setIconBackgroundColor(iconBackground)
        } else {
            vImage.setImage(imageResId)
            vIcon.visibility = View.GONE
            vImage.visibility = View.VISIBLE
        }


        vTouch.setOnClickListener { onClick.invoke() }
    }

    companion object {

        fun instanceIcon(@StringRes title: Int, @DrawableRes iconId: Int, onClick: ()->Unit): CardCategory {
            return CardCategory(ToolsResources.s(title), 0, iconId, null, onClick)
        }

        fun instanceIcon(@StringRes title: Int, @DrawableRes iconId: Int, iconBackground: Int?, onClick:  ()->Unit): CardCategory {
            return CardCategory(ToolsResources.s(title), 0, iconId, iconBackground, onClick)
        }
    }

}

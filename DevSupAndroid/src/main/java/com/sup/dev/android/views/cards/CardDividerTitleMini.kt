package com.sup.dev.android.views.cards

import androidx.annotation.StringRes
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources

class CardDividerTitleMini constructor(
    private var title: String? = null
) : Card(R.layout.card_divider_title_mini) {

    private var background: Int = 0
    private var enabled = true
    private var dividerBottom = true
    private var dividerTop = false
    private var gravity = Gravity.LEFT

    constructor(@StringRes title: Int) : this(ToolsResources.s(title))

    override fun bindView(view: View) {
        super.bindView(view)
        val vText = view.findViewById<TextView>(R.id.vText)
        val vDividerTop = view.findViewById<View>(R.id.vDividerTop)
        val vDividerBottom = view.findViewById<View>(R.id.vDividerBottom)

        vDividerTop.visibility = if (dividerTop) View.VISIBLE else View.INVISIBLE
        vDividerBottom.visibility = if (dividerBottom) View.VISIBLE else View.INVISIBLE
        if (background != 0) view.setBackgroundColor(background)

        vText.text = title
        vText.isEnabled = isEnabled()
        (vText.layoutParams as FrameLayout.LayoutParams).gravity = gravity
    }

    //
    //  Setters
    //

    fun setEnabled(enabled: Boolean): CardDividerTitleMini {
        this.enabled = enabled
        update()
        return this
    }

    fun setBackground(background: Int): CardDividerTitleMini {
        this.background = background
        update()
        return this
    }

    fun setText(@StringRes title: Int): CardDividerTitleMini {
        return setText(ToolsResources.s(title))
    }

    fun setText(title: String?): CardDividerTitleMini {
        this.title = title
        update()
        return this
    }

    fun setDividerBottom(divider: Boolean): CardDividerTitleMini {
        this.dividerBottom = divider
        update()
        return this
    }

    fun setDividerTop(divider: Boolean): CardDividerTitleMini {
        this.dividerTop = divider
        update()
        return this
    }

    fun toCenter(): CardDividerTitleMini {
        this.gravity = Gravity.CENTER
        update()
        return this
    }

    //
    //  Getters
    //

    fun isEnabled(): Boolean {
        return enabled
    }
}

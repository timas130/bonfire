package com.sup.dev.android.views.cards

import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources

class CardTitleMini @JvmOverloads constructor(title: String? = null) : Card(R.layout.card_title_mini) {

    private var dividerVisible = false
    private var background = 0
    private var enabled = true

    private var title: String? = null
    private var customColor: Boolean = false
    private var textColor: Int = 0
    private var textGravity = Gravity.LEFT

    constructor(@StringRes title: Int) : this(ToolsResources.s(title)) {}

    init {
        setTitle(title)
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vDivider = view.findViewById<View>(R.id.vDivider)
        val textView = view.findViewById<TextView>(R.id.vText)

        vDivider.visibility = if (dividerVisible) View.VISIBLE else View.GONE
        if (background != 0) view.setBackgroundColor(background)
        textView.text = title
        textView.isEnabled = enabled
        textView.gravity = textGravity

        if (customColor) textView.setTextColor(textColor)
    }

    //
    //  Setters
    //

    //  Gravity.LEFT
    fun setTextGravity(textGravity: Int): CardTitleMini {
        this.textGravity = textGravity
        update()
        return this
    }

    fun setDividerVisible(dividerVisible: Boolean): CardTitleMini {
        this.dividerVisible = dividerVisible
        update()
        return this
    }

    fun setEnabled(enabled: Boolean): CardTitleMini {
        this.enabled = enabled
        update()
        return this
    }

    fun setBackground(background: Int): CardTitleMini {
        this.background = background
        update()
        return this
    }

    fun setTitle(@StringRes title: Int): CardTitleMini {
        return setTitle(ToolsResources.s(title))
    }

    fun setTitle(title: String?): CardTitleMini {
        this.title = title
        update()
        return this
    }

    fun setTitleColor(color: Int): CardTitleMini {
        customColor = true
        textColor = color
        update()
        return this
    }


}

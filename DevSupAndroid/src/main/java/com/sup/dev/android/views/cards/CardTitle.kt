package com.sup.dev.android.views.cards

import android.view.Gravity
import androidx.annotation.StringRes
import android.view.View
import android.widget.TextView
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources

class CardTitle @JvmOverloads constructor(title: String? = null) : Card(R.layout.card_title) {

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
    fun setTextGravity(textGravity: Int): CardTitle {
        this.textGravity = textGravity
        update()
        return this
    }

    fun setDividerVisible(dividerVisible: Boolean): CardTitle {
        this.dividerVisible = dividerVisible
        update()
        return this
    }

    fun setEnabled(enabled: Boolean): CardTitle {
        this.enabled = enabled
        update()
        return this
    }

    fun setBackground(background: Int): CardTitle {
        this.background = background
        update()
        return this
    }

    fun setTitle(@StringRes title: Int): CardTitle {
        return setTitle(ToolsResources.s(title))
    }

    fun setTitle(title: String?): CardTitle {
        this.title = title
        update()
        return this
    }

    fun setTitleColor(color: Int): CardTitle {
        customColor = true
        textColor = color
        update()
        return this
    }


}

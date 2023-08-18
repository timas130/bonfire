package com.sup.dev.android.views.settings

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources

class SettingsArrow @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : Settings(context, attrs) {

    private val vArrow: ImageView = ImageView(context)

    init {

        vArrow.setImageDrawable(ToolsResources.getDrawable(R.drawable.ic_keyboard_arrow_right_white_24dp))
        vArrow.setColorFilter(ToolsResources.getColorAttr(R.attr.colorOnPrimaryIcons))

        setSubView(vArrow)
    }

    fun setArrowIcon(@DrawableRes drawable: Int) {
        setArrowIcon(ToolsResources.getDrawable(drawable))
    }

    fun setArrowIcon(drawable: Drawable) {
        vArrow.setImageDrawable(drawable)
    }


}
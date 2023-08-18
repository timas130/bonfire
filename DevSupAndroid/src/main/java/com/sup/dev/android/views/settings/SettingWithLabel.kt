package com.sup.dev.android.views.settings

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsView

class SettingWithLabel @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : Settings(context, attrs) {

    private val vLabel: TextView = ToolsView.inflate(R.layout.z_text_caption)

    init {
        setSubView(vLabel)
        vLabel.gravity = Gravity.RIGHT
    }


    fun setLabel(string: String) {
        vLabel.text = string
    }


}
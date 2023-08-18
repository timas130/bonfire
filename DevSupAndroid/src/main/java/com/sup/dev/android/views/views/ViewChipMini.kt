package com.sup.dev.android.views.views

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.layouts.LayoutCorned

class ViewChipMini @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LayoutCorned(context, attrs) {

    private val vText: TextView = ToolsView.inflate(context, R.layout.z_text_caption)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ViewChipMini)
        val text = a.getString(R.styleable.ViewChipMini_android_text) ?: ""
        val textColor = a.getColor(R.styleable.ViewChipMini_android_textColor, 0)
        a.recycle()

        if (textColor != 0) vText.setTextColor(textColor)
        setChipMode(true)
        setCircleMode(true)
        setText(text)

        addView(vText)

        vText.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        vText.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        (vText.layoutParams as LayoutParams).gravity = Gravity.CENTER
        setSizeNormal()
    }

    //
    //  Setters
    //

    fun setText(text: String?) {
        vText.text = text
        visibility = if (text == null || text.isEmpty()) View.GONE else View.VISIBLE
     }

    fun setSizeNormal(){
        vText.textSize = 9f
        (vText.layoutParams as LayoutParams).leftMargin = ToolsView.dpToPx(4).toInt()
        (vText.layoutParams as LayoutParams).rightMargin = ToolsView.dpToPx(4).toInt()
        if(layoutParams != null && layoutParams.height>0) layoutParams.height = ToolsView.dpToPx(18).toInt()
    }

    fun setSizeMini(){
        vText.textSize = 4f
        (vText.layoutParams as LayoutParams).leftMargin = ToolsView.dpToPx(1).toInt()
        (vText.layoutParams as LayoutParams).rightMargin = ToolsView.dpToPx(1).toInt()
        if(layoutParams != null && layoutParams.height>0) layoutParams.height = ToolsView.dpToPx(9).toInt()
    }

    //
    //  Getters
    //

    fun getText() = vText.text

}
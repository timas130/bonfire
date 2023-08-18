package com.sup.dev.android.views.views.layouts

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout


class LayoutFrameMeasureCallback @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    var onMeasure: (Int, Int) -> Unit = {_,_ -> }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        onMeasure.invoke(widthMeasureSpec, heightMeasureSpec)
    }


}
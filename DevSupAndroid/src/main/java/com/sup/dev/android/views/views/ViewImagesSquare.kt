package com.sup.dev.android.views.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

class ViewImagesSquare constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ImageView(context, attrs) {

    override fun onMeasure(width: Int, height: Int) {
        super.onMeasure(width, height)
        setMeasuredDimension(measuredWidth, measuredWidth)
    }

}
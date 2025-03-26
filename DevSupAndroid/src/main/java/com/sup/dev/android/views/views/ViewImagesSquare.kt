package com.sup.dev.android.views.views

import android.content.Context
import android.util.AttributeSet

class ViewImagesSquare constructor(
    context: Context,
    attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatImageView(context, attrs) {

    override fun onMeasure(width: Int, height: Int) {
        super.onMeasure(width, height)
        setMeasuredDimension(measuredWidth, measuredWidth)
    }

}

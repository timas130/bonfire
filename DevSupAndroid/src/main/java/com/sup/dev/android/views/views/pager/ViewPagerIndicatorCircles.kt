package com.sup.dev.android.views.views.pager

import android.content.Context
import android.graphics.Canvas
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet

class ViewPagerIndicatorCircles(context: Context, attrs: AttributeSet) : ViewPagerIndicator(context, attrs), ViewPager.OnPageChangeListener {

    init {
        setWillNotDraw(false)
    }

    override fun onChanged() {
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var count = if (isInEditMode) 3 else if (pager != null) pager!!.adapter!!.count else 1
        if (count == 0) count = 1
        val r = (height / 2).toFloat()
        val w = offset * (count - 1) + r * 2f * count.toFloat()
        var x = (width - w) / 2 + r
        val a = r * 2 + offset

        paint.color = color
        for (i in 0 until count) {
            canvas.drawCircle(x, r, r, paint)
            x += a
        }

        paint.color = colorSelected

        x = (width - w) / 2 + r + a * position
        val off = positionOffset * a
        canvas.drawCircle(x + off, r, r, paint)
        var xx = x
        if (off > a / 2)
            xx += (off - a / 2) * 2
        canvas.drawCircle(xx, r, r, paint)

        canvas.drawRect(xx, 0f, x + off, height.toFloat(), paint)
    }
}

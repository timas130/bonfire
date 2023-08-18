package com.sup.dev.android.views.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView

class ViewProgressCircle @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint: Paint
    private var progressPercent = 0f

    init {
        SupAndroid.initEditMode(this)

        paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = ToolsView.dpToPx(2)
        paint.color = ToolsResources.getSecondaryColor(context)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (progressPercent > 0L && canvas != null) {

            val x = paint.strokeWidth
            val y = paint.strokeWidth
            val w = width - paint.strokeWidth
            val h = height - paint.strokeWidth
            val r = 360f  * (progressPercent/100f)

            canvas.drawArc(x, y, w, h, 270f, r, false, paint)
            invalidate()
        }
    }

    fun setProgress(value: Long, max: Long) {
        setProgress(100f * (value.toFloat() / max))
    }

    fun setProgress(value: Int, max: Int) {
        setProgress(100f * (value.toFloat() / max))
    }

    fun setProgress(value: Float, max: Float) {
        setProgress(100f / (max / value))
    }

    fun setProgress(percent: Float) {
        this.progressPercent = percent
        invalidate()
    }

    fun setProgressColorRes(res: Int) {
        setProgressColor(ToolsResources.getColor(res))
    }

    fun setProgressColor(color: Int) {
        paint.color = color
        invalidate()
    }


}

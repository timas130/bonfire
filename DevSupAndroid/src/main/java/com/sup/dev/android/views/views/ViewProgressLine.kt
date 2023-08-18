package com.sup.dev.android.views.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources

class ViewProgressLine @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint: Paint
    private val path: Path

    private var progressPercent = 0f
    private var colorProgress = 0
    private var colorBackground = 0

    init {

        SupAndroid.initEditMode(this)
        colorBackground = ToolsResources.getColor(R.color.focus)
        colorProgress = ToolsResources.getSecondaryColor(context)


        val a = context.obtainStyledAttributes(attrs, R.styleable.ViewProgressLine, 0, 0)
        val progress = a.getInteger(R.styleable.ViewProgressLine_ViewProgressLine_progress, 0)
        colorProgress = a.getColor(R.styleable.ViewProgressLine_ViewProgressLine_color, colorProgress)
        colorBackground = a.getColor(R.styleable.ViewProgressLine_ViewProgressLine_colorBackground, colorBackground)
        a.recycle()

        path = Path()
        paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL

        setProgress(progress.toFloat())
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        recreateChip()
    }

    private fun recreateChip() {

        path.reset()

        path.addArc(RectF(0f, 0f, height.toFloat(), height.toFloat()), 90f, 180f)
        path.addArc(RectF((width - height).toFloat(), 0f, width.toFloat(), height.toFloat()), 270f, 180f)
        path.addRect((height / 2).toFloat(), 0f, (width - height / 2).toFloat(), height.toFloat(), Path.Direction.CCW)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val r = Math.min(width, height) / 2

        paint.color = colorBackground
        canvas.drawPath(path, paint)

        if (progressPercent > 0) {
            paint.color = colorProgress

            val end = (width - r) / 100f * (if (progressPercent > 100) 100f else progressPercent)

            canvas.drawCircle(r.toFloat(), r.toFloat(), r.toFloat(), paint)
            if (end > r) {
                canvas.drawCircle(end, r.toFloat(), r.toFloat(), paint)
                canvas.drawRect(r.toFloat(), 0f, end, height.toFloat(), paint)
            }
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

    fun setLineColorR(res: Int) {
        setLineColor(ToolsResources.getColor(res))
    }

    fun setLineColor(color: Int) {
        colorProgress = color
        invalidate()
    }

    fun setLineBackgroundColorR(res: Int) {
        setLineBackgroundColor(ToolsResources.getColor(res))
    }

    fun setLineBackgroundColor(color: Int) {
        colorBackground = color
        invalidate()
    }


}

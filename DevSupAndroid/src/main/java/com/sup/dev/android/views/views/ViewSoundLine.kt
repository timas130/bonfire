package com.sup.dev.android.views.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources

class ViewSoundLine @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var color = ToolsResources.getColorAttr(R.attr.colorStroke)
    private var colorProgress = ToolsResources.getSecondaryColor(context)
    private val maxLines = 30
    private val paint = Paint()
    private var soundMask = Array(0) { 0 }
    private var maxValue = 0
    private var progress = 0f

    init {
        paint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (soundMask.isEmpty()) return

        val count = maxLines
        val w = width / count
        val r = w / 2f
        var x = 0f
        val h = (height - r * 2f) / 2f

        paint.style = Paint.Style.FILL
        for (i in soundMask.indices) {

            if (i >= progress) paint.color = color
            else paint.color = colorProgress

            val hh = soundMask[i] * (h / maxValue)
            canvas.drawCircle(x + r, h - hh + r, r, paint)
            canvas.drawCircle(x + r, h + hh - r, r, paint)
            canvas.drawRect(x, h - hh + r, x + w, h + hh - r, paint)
            x += w + 1
            if (x + w >= width) break
        }

    }

    fun setSoundMask(soundMask: Array<Int>) {
        this.soundMask = soundMask

        var minValue = Integer.MAX_VALUE
        for (i in soundMask.indices) {
            if (soundMask[i] < minValue) minValue = soundMask[i]
            if (soundMask[i] > maxValue) maxValue = soundMask[i]
        }
        for (i in soundMask.indices) soundMask[i] -= minValue

        invalidate()
    }

    fun setColor(color: Int) {
        this.color = color
        invalidate()
    }

    fun setColorProgress(color: Int) {
        this.colorProgress = color
        invalidate()
    }

    fun setProgress(progress: Float, max: Float) {
        this.progress = soundMask.size * (progress / max)
        invalidate()
    }


}

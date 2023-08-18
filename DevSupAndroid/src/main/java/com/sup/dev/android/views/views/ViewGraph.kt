package com.sup.dev.android.views.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.HorizontalScrollView
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsTextAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java.tools.ToolsThreads
import java.util.ArrayList


class ViewGraph @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint()
    private val points = ArrayList<Float>()
    private var vScroll: HorizontalScrollView? = null

    private val offsetViewBottom = ToolsView.dpToPx(24)
    private val offsetViewLeft = ToolsView.dpToPx(24)
    private val offsetViewTop = ToolsView.dpToPx(24)
    private var minPointsCount = 0
    private var maxPointsCount = 0
    private var minTopY = 0
    private var greedYSize = ToolsView.dpToPx(1)
    private var greedYFrequency = 10
    private var greedXSize = ToolsView.dpToPx(1)
    private var greedXFrequency = 10
    private var greedColor = ToolsResources.getColor(R.color.grey_400)
    private val pointSize = ToolsView.dpToPx(2)
    private val pointColor = ToolsResources.getColor(R.color.red_700)
    private val lineSize = ToolsView.dpToPx(4)
    private val lineColor = ToolsResources.getColor(R.color.red_700)
    private val textSize = ToolsView.spToPx(8)

    private var providerMaskX: ((Float)->String)? = null
    private var providerMaskY: ((Float)->String)? = null

    init {
        paint.isAntiAlias = true
        paint.textSize = textSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var h = 0f
        val wPoints = (if (points.size > minPointsCount) points.size else minPointsCount).toFloat()
        for (d in points) h = if (h > d) h else d
        h = if (h > minTopY) h else minTopY.toFloat()
        val cellSizeH = (height - offsetViewBottom - offsetViewTop) / h
        val cellSizeW = (width - offsetViewLeft) / wPoints

        if (greedXSize > 0 || greedYSize > 0) {
            paint.color = greedColor
            paint.strokeWidth = ToolsMath.max(greedXSize, greedYSize)
            canvas.drawLine(offsetViewLeft, offsetViewTop, width.toFloat(), offsetViewTop, paint)
            canvas.drawLine(offsetViewLeft, offsetViewTop, offsetViewLeft, (height - offsetViewBottom), paint)
            canvas.drawLine(width.toFloat(), (height - offsetViewBottom), offsetViewLeft, (height - offsetViewBottom), paint)
            canvas.drawLine(width.toFloat(), (height - offsetViewBottom), width.toFloat(), offsetViewTop, paint)
        }

        if (greedYSize > 0) {
            paint.color = greedColor
            paint.strokeWidth = greedYSize
            var y = 0f
            while (y < h + 1) {
                canvas.drawLine(offsetViewLeft, cellSizeH * y + offsetViewTop, width.toFloat(), cellSizeH * y + offsetViewTop, paint)
                if (y < h)
                    canvas.drawText(providerMaskY!!.invoke(h - y),
                            offsetViewLeft - ToolsTextAndroid.getStringWidth(paint.typeface, textSize, providerMaskY!!.invoke(h - y)) - ToolsView.dpToPx(4).toFloat(),
                            cellSizeH * y + ToolsTextAndroid.getStringHeight(paint.typeface, textSize) / 2 + offsetViewTop,
                            paint)
                y += greedYFrequency
            }

        }

        if (greedXSize > 0) {
            paint.color = greedColor
            paint.strokeWidth = greedXSize
            var x = 0f
            while (x < wPoints + 1) {
                canvas.drawLine(cellSizeW * x + offsetViewLeft, offsetViewTop, cellSizeW * x + offsetViewLeft, (height - offsetViewBottom), paint)
                if (x > 0)
                    canvas.drawText(providerMaskX!!.invoke(x),
                            cellSizeW * x - ToolsTextAndroid.getStringWidth(paint.typeface, textSize, providerMaskX!!.invoke(x)) / 2 + offsetViewLeft,
                            (height - offsetViewBottom) + ToolsTextAndroid.getStringHeight(paint.typeface, textSize) + ToolsView.dpToPx(4),
                            paint)
                x += greedXFrequency
            }
        }

        if (lineSize > 0) {
            paint.strokeWidth = lineSize
            paint.color = lineColor
            for (i in 1 until points.size) {
                val pX = (i - 1) * cellSizeW + cellSizeW / 2 + offsetViewLeft
                val pY = cellSizeH * h - points[i - 1] * cellSizeH - cellSizeH / 2 + offsetViewTop
                val x = i * cellSizeW + cellSizeW / 2 + offsetViewLeft
                val y = cellSizeH * h - points[i] * cellSizeH - cellSizeH / 2 + offsetViewTop
                canvas.drawLine(pX, pY, x, y, paint)
            }
        }

        if (pointSize > 0) {
            paint.color = pointColor
            for (i in points.indices) {
                val x = i * cellSizeW + cellSizeW / 2 + offsetViewLeft
                val y = cellSizeH * h - points[i] * cellSizeH - cellSizeH / 2 + offsetViewTop
                canvas.drawCircle(x, y, pointSize, paint)
            }
        }

    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = vScroll!!.measuredWidth
        if (w == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val wPoints:Float = if (points.size > minPointsCount) points.size.toFloat() else minPointsCount.toFloat()
        val cellSizeW = (w - offsetViewLeft) / if (wPoints < maxPointsCount) wPoints else maxPointsCount.toFloat()

        super.onMeasure(View.MeasureSpec.makeMeasureSpec((cellSizeW * wPoints + offsetViewLeft).toInt(), View.MeasureSpec.EXACTLY), heightMeasureSpec)

        ToolsThreads.main(true) {
            if (vScroll != null && measuredWidth - (vScroll!!.scrollX + vScroll!!.width) < ToolsView.dpToPx(15))
                vScroll!!.scrollTo(measuredWidth, vScroll!!.scrollY)
        }
    }

    fun clear() {
        points.clear()
        requestLayout()
        invalidate()
    }

    fun addPoint(value: Float) {
        points.add(value)
        requestLayout()
        invalidate()
    }

    //
    //  Setters
    //


    fun setScroll(vScroll: HorizontalScrollView) {
        this.vScroll = vScroll
        requestLayout()
    }

    fun setMaxX(maxPointsCount: Int) {
        this.maxPointsCount = maxPointsCount
        requestLayout()
    }

    fun setMinY(minTopY: Int) {
        this.minTopY = minTopY
        requestLayout()
    }

    fun setMinX(minPointsCount: Int) {
        this.minPointsCount = minPointsCount
        requestLayout()
    }

    fun setYFrequency(greedYFrequency: Int) {
        this.greedYFrequency = greedYFrequency
        requestLayout()
    }

    fun setGreedYSize(greedYSize: Int) {
        this.greedYSize = greedYSize.toFloat()
        requestLayout()
    }

    fun setXFrequency(greedXFrequency: Int) {
        this.greedXFrequency = greedXFrequency
        requestLayout()
    }

    fun setGreedXSize(greedXSize: Int) {
        this.greedXSize = greedXSize.toFloat()
        requestLayout()
    }

    fun setGreedColor(greedColor: Int) {
        this.greedColor = greedColor
        requestLayout()
    }

    fun setProviderMaskY(providerMaskY: (Float) -> String) {
        this.providerMaskY = providerMaskY
        requestLayout()
    }

    fun setProviderMaskX(providerMaskX: (Float) -> String) {
        this.providerMaskX = providerMaskX
        requestLayout()
    }
}
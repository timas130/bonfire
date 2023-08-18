package com.sup.dev.android.views.views.layouts

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.tools.ToolsThreads

class LayoutMirrorSize @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    companion object {
        val MODE_BIGGER_WH = 1
        val MODE_SMALLER_WH = 2
        val MODE_WHOM_BIGGER_W = 3
        val MODE_WHOM_SMALLER_W = 4
        val MODE_WHOM_BIGGER_H = 5
        val MODE_WHOM_SMALLER_H = 6
        val MODE_USE_1_IF_NO_ZERO = 7
        val MODE_USE_2_IF_NO_ZERO = 8
    }

    private var multiViewMode = MODE_USE_1_IF_NO_ZERO
    private var mirrorViewId = 0
    private var mirrorViewId_2 = 0
    private var wPercent = 100f
    private var hPercent = 100f
    private var mirrorView: View? = null
    private var mirrorView_2: View? = null

    private var w: Int = 0
    private var h: Int = 0

    init {

        SupAndroid.initEditMode(this)

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.LayoutMirrorSize, 0, 0)
        mirrorViewId = a.getResourceId(R.styleable.LayoutMirrorSize_LayoutMirrorSize_mirrorView, mirrorViewId)
        mirrorViewId_2 = a.getResourceId(R.styleable.LayoutMirrorSize_LayoutMirrorSize_mirrorView_2, mirrorViewId_2)
        wPercent = a.getInteger(R.styleable.LayoutMirrorSize_LayoutMirrorSize_wPercent, wPercent.toInt()).toFloat()
        hPercent = a.getInteger(R.styleable.LayoutMirrorSize_LayoutMirrorSize_hPercent, hPercent.toInt()).toFloat()
        multiViewMode = a.getInt(R.styleable.LayoutMirrorSize_LayoutMirrorSize_mode, multiViewMode)
        a.recycle()

    }

    fun setMirrorView(mirrorView: View?) {
        this.mirrorView = mirrorView
        if (mirrorView == null) return
        mirrorView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> ToolsThreads.main(true) { requestLayout() } }
        requestLayout()
    }

    fun setMirrorView_2(mirrorView_2: View?) {
        this.mirrorView_2 = mirrorView_2
        if (mirrorView_2 == null) return
        mirrorView_2.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> ToolsThreads.main(true) { requestLayout() } }
        requestLayout()
    }

    fun setMode(mode: Int) {
        multiViewMode = mode
        requestLayout()
    }

    private fun prepareSizes(w: Float, h: Float) {
        this.w = (w / 100 * wPercent).toInt()
        this.h = (h / 100 * hPercent).toInt()
    }

    private fun mirrorViewAvailable(): Boolean {
        return mirrorView != null && mirrorView!!.visibility == View.VISIBLE
    }

    private fun mirrorView_2_Available(): Boolean {
        return mirrorView_2 != null && mirrorView_2!!.visibility == View.VISIBLE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        if (mirrorView == null && mirrorViewId != 0)
            setMirrorView(ToolsView.findViewOnParents(this, mirrorViewId))
        if (mirrorView_2 == null && mirrorViewId_2 != 0)
            setMirrorView_2(ToolsView.findViewOnParents(this, mirrorViewId_2))

        if (!mirrorViewAvailable() && !mirrorView_2_Available()) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY))
            return
        }

        w = MeasureSpec.getSize(widthMeasureSpec)
        h = MeasureSpec.getSize(heightMeasureSpec)


        if (mirrorViewAvailable() && !mirrorView_2_Available())
            prepareSizes(mirrorView!!.measuredWidth.toFloat(), mirrorView!!.measuredHeight.toFloat())

        if (!mirrorViewAvailable() && mirrorView_2_Available())
            prepareSizes(mirrorView_2!!.measuredWidth.toFloat(), mirrorView_2!!.measuredHeight.toFloat())


        if (mirrorViewAvailable() && mirrorView_2_Available()) {

            val w1 = mirrorView!!.measuredWidth.toFloat()
            val h1 = mirrorView!!.measuredHeight.toFloat()
            val w2 = mirrorView_2!!.measuredWidth.toFloat()
            val h2 = mirrorView_2!!.measuredHeight.toFloat()

            if (multiViewMode == MODE_BIGGER_WH) prepareSizes(Math.max(w1, w2), Math.max(h1, h2))
            if (multiViewMode == MODE_SMALLER_WH) prepareSizes(Math.min(w1, w2), Math.min(h1, h2))
            if (multiViewMode == MODE_WHOM_BIGGER_W)
                prepareSizes(if (w2 > w1) w2 else w1, if (w2 > w1) h2 else h1)
            if (multiViewMode == MODE_WHOM_SMALLER_W)
                prepareSizes(if (w1 < w2) w1 else w2, if (w1 < w2) h1 else h2)
            if (multiViewMode == MODE_WHOM_BIGGER_H)
                prepareSizes(if (h2 > h1) w2 else w1, if (h2 > h1) h2 else h1)
            if (multiViewMode == MODE_WHOM_SMALLER_H)
                prepareSizes(if (h1 < h2) w1 else w2, if (h1 < h2) h1 else h2)
            if (multiViewMode == MODE_USE_1_IF_NO_ZERO)
                prepareSizes(if (w1 != 0f) w1 else w2, if (h1 != 0f) h1 else h2)
            if (multiViewMode == MODE_USE_2_IF_NO_ZERO)
                prepareSizes(if (w2 != 0f) w2 else w1, if (h2 != 0f) h2 else h1)

        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY))


    }

}

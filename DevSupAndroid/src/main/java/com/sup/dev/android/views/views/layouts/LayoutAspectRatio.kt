package com.sup.dev.android.views.views.layouts

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.ViewGroup
import android.widget.FrameLayout
import com.sup.dev.android.R

open class LayoutAspectRatio constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private var rw = 0f
    private var rh = 0f

    init {

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.LayoutAspectRatio, 0, 0)
        rw = a.getFloat(R.styleable.LayoutAspectRatio_LayoutAspectRatio_w, rw)
        rh = a.getFloat(R.styleable.LayoutAspectRatio_LayoutAspectRatio_h, rh)
        a.recycle()

    }
    fun setRatio(rw: Int, rh: Int) {
        setRatio(rw.toFloat(), rh.toFloat())
    }

    fun setRatio(rw: Float, rh: Float) {
        this.rw = rw
        this.rh = rh
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        if (rw <= 0 || rh <= 0) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY))
            return
        }

        if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        var w = MeasureSpec.getSize(widthMeasureSpec)
        var h = MeasureSpec.getSize(heightMeasureSpec)
        val wm = MeasureSpec.getMode(widthMeasureSpec)
        val hm = MeasureSpec.getMode(heightMeasureSpec)

        if (wm == UNSPECIFIED) w = h
        if (hm == UNSPECIFIED) h = w

        val aw = w / rw
        val ah = h / rh
        val insW = rw * if (ah < aw) ah else aw
        val insH = rh * if (ah < aw) ah else aw

        super.onMeasure(MeasureSpec.makeMeasureSpec(insW.toInt(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(insH.toInt(), MeasureSpec.EXACTLY))
    }

}

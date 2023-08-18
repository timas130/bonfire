package com.sup.dev.android.views.views.layouts

import android.content.Context
import androidx.annotation.MainThread
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.sup.dev.android.R


class LayoutFlow @MainThread
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    private var mHorizontalSpacing = 0
    private var mVerticalSpacing = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LayoutFlow)
        mHorizontalSpacing = a.getDimensionPixelSize(R.styleable.LayoutFlow_LayoutFlow_horizontal, 0)
        mVerticalSpacing = a.getDimensionPixelSize(R.styleable.LayoutFlow_LayoutFlow_vertical, 0)
        a.recycle()
    }

    fun setHorizontalSpacing(mHorizontalSpacing: Int) {
        this.mHorizontalSpacing = mHorizontalSpacing
    }

    fun setVerticalSpacing(mVerticalSpacing: Int) {
        this.mVerticalSpacing = mVerticalSpacing
    }

    @MainThread
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthLimit = MeasureSpec.getSize(widthMeasureSpec) - paddingRight
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val growHeight = widthMode != MeasureSpec.UNSPECIFIED
        var width = 0
        var currentWidth = paddingLeft
        var currentHeight = paddingTop
        var maxChildHeight = 0
        var breakLine = false
        var newLine = false
        var spacing = 0

        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            val lp = child.layoutParams as LayoutParams
            spacing = mHorizontalSpacing

            if (lp.horizontalSpacing >= 1) spacing = lp.horizontalSpacing

            if (growHeight && (breakLine || currentWidth + child.measuredWidth > widthLimit)) {
                newLine = true
                currentHeight += maxChildHeight + mVerticalSpacing

                width = Math.max(width, currentWidth - spacing)

                currentWidth = paddingLeft
                maxChildHeight = 0
            } else {
                newLine = false
            }

            maxChildHeight = Math.max(maxChildHeight, child.measuredHeight)

            lp.x = currentWidth
            lp.y = currentHeight

            currentWidth += child.measuredWidth + spacing

            breakLine = lp.breakLine
        }

        if (!newLine)
            width = Math.max(width, currentWidth - spacing)
        width += paddingRight
        val height = currentHeight + maxChildHeight + paddingBottom

        setMeasuredDimension(View.resolveSize(width, widthMeasureSpec),
                View.resolveSize(height, heightMeasureSpec))

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams
            child.layout(lp.x, lp.y, lp.x + child.measuredWidth, lp.y + child.measuredHeight)
        }
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams {
        return LayoutParams(p.width, p.height)
    }


    class LayoutParams : ViewGroup.LayoutParams {
        var horizontalSpacing: Int = 0
        var breakLine: Boolean = false
        internal var x: Int = 0
        internal var y: Int = 0

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.LayoutFlow_LayoutParams)
            try {
                horizontalSpacing = a.getDimensionPixelSize(R.styleable.LayoutFlow_LayoutParams_LayoutFlow_LayoutParams_horizontalSpacing, 0)
                breakLine = a.getBoolean(R.styleable.LayoutFlow_LayoutParams_LayoutFlow_LayoutParams_horizontalSpacing, false)
            } finally {
                a.recycle()
            }
        }

        constructor(w: Int, h: Int) : super(w, h) {}
    }
}
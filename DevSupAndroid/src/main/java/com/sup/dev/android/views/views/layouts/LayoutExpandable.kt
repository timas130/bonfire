package com.sup.dev.android.views.views.layouts

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import com.sup.dev.android.R


class LayoutExpandable @MainThread
constructor(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var mWidth = 0
    private var mHeight = 0
    private var mDuration = 0
    private var mExpandDuration = 0
    private var mCollapseDuration = 0

    private var mIsInited = false
    private var mIsExpand = false
    private var mIsExecuting = false
    private var mIsClickToToggle = false

    private var mSwitcher: View? = null
    private var mListener: OnStateChangedListener? = null
    private var mExpandInterpolator: Interpolator? = null
    private var mCollapseInterpolator: Interpolator? = null


    init {

        val a = context.obtainStyledAttributes(attrs, R.styleable.LayoutExpandable)
        mDuration = a.getInt(R.styleable.LayoutExpandable_LayoutExpandable_duration, DEFAULT_DURATION)
        mIsClickToToggle = a.getBoolean(R.styleable.LayoutExpandable_LayoutExpandable_clickToToggle, false)
        mIsExpand = a.getBoolean(R.styleable.LayoutExpandable_LayoutExpandable_expanded, true)
        a.recycle()

        mExpandDuration = if (mDuration == 0) DEFAULT_DURATION else mDuration
        mCollapseDuration = if (mDuration == 0) DEFAULT_DURATION else mDuration
        if (mIsClickToToggle)  setOnClickListener { toggle() }
        else setOnClickListener(null)
    }

    //
    //  Expand control
    //

    @MainThread
    fun expand() {
        if (mIsExpand || mIsExecuting) return

        executeExpand(this)
        startSwitcherAnimation()
    }

    @MainThread
    fun collapse() {
        if (!mIsExpand || mIsExecuting) return

        executeCollapse(this)
        startSwitcherAnimation()
    }

    @MainThread
    fun toggle() {
        if (mIsExpand)
            collapse()
        else
            expand()

    }

    //
    //  Methods
    //

    @MainThread
    private fun startSwitcherAnimation() {
        if (mSwitcher != null) {
            val duration = if (mDuration == 0) DEFAULT_DURATION else if (mIsExpand) mExpandDuration else mCollapseDuration
            val rotateAnimation = createRotateAnimation(mSwitcher!!, duration)
            mSwitcher!!.startAnimation(rotateAnimation)
        }
    }

    @MainThread
    private fun measureChildWidth(): Int {
        var width = 0
        val cnt = childCount
        for (i in 0 until cnt) width += getChildAt(i).measuredWidth

        return width
    }

    @MainThread
    private fun measureChildHeight(): Int {
        var height = 0
        val cnt = childCount
        for (i in 0 until cnt) height += getChildAt(i).measuredHeight

        return height
    }

    @MainThread
    private fun measureDimension(defaultSize: Int, measureSpec: Int): Int {
        val result: Int

        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        when (specMode) {
            View.MeasureSpec.UNSPECIFIED -> result = specSize
            View.MeasureSpec.EXACTLY -> result = specSize
            View.MeasureSpec.AT_MOST -> result = Math.min(defaultSize, specSize)
            else -> result = specSize
        }

        return result
    }

    @MainThread
    private fun createRotateAnimation(view: View, duration: Int): RotateAnimation {
        val pivotX = view.width shr 1
        val pivotY = view.height shr 1
        val animation = RotateAnimation((if (mIsExpand) 0 else -180).toFloat(), (if (mIsExpand) -180 else 0).toFloat(), pivotX.toFloat(), pivotY.toFloat())
        animation.duration = duration.toLong()
        animation.fillAfter = true
        return animation
    }

    @MainThread
    private fun createAnimator(view: View, startPos: Int, endPos: Int): ValueAnimator {
        val isExpand = startPos < endPos
        val duration = if (mDuration == 0) DEFAULT_DURATION else if (isExpand) mExpandDuration else mCollapseDuration
        return this.createAnimator(view, startPos, endPos, duration)
    }

    @MainThread
    private fun createAnimator(view: View, startPos: Int, endPos: Int, duration: Int): ValueAnimator {
        val animator = ValueAnimator.ofInt(startPos, endPos)
        animator.duration = duration.toLong()
        animator.addUpdateListener { animation ->
            val newPos = animation.animatedValue as Int
            val orientation = (view as LayoutExpandable).orientation
            val params = view.getLayoutParams()
            if (LinearLayout.HORIZONTAL == orientation) {
                params.width = newPos
                params.height = measuredHeight
                view.setLayoutParams(params)
            } else if (LinearLayout.VERTICAL == orientation) {
                params.width = measuredWidth
                params.height = newPos
                view.setLayoutParams(params)
            }
        }
        return animator
    }

    @MainThread
    private fun executeExpand(view: View) {
        mIsExpand = !mIsExpand
        visibility = View.VISIBLE
        val newPos = if (orientation == LinearLayout.HORIZONTAL) mWidth else mHeight
        val animator = createAnimator(view, 0, newPos)
        animator.interpolator = mExpandInterpolator
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                setExecuting(true)
                if (mListener != null) {
                    mListener!!.onPreExpand()
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                setExecuting(false)
                if (mListener != null) {
                    mListener!!.onExpanded()
                }
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        animator.start()
    }

    @MainThread
    private fun executeCollapse(view: View) {
        mIsExpand = !mIsExpand
        val newPos = if (orientation == LinearLayout.HORIZONTAL) mWidth else mHeight
        val animator = createAnimator(view, newPos, 0)
        animator.interpolator = mCollapseInterpolator
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                setExecuting(true)
                if (mListener != null) {
                    mListener!!.onPreCollapse()
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                setExecuting(false)
                visibility = View.GONE
                if (mListener != null) {
                    mListener!!.onCollapsed()
                }
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        animator.start()
    }

    //
    //  Setters
    //
    @AnyThread
    fun setClickToToggle(isClickToToggle: Boolean) {
        mIsClickToToggle = isClickToToggle
    }

    @AnyThread
    fun setOnStateChangedListener(l: OnStateChangedListener) {
        mListener = l
    }

    @AnyThread
    fun setDuration(duration: Int) {
        mDuration = duration
        this.setExpandDuration(duration)
        this.setCollapseDuration(duration)
    }

    @AnyThread
    fun setExpandDuration(expandDuration: Int) {
        mExpandDuration = expandDuration
    }

    @AnyThread
    fun setCollapseDuration(collapseDuration: Int) {
        mCollapseDuration = collapseDuration
    }

    @MainThread
    fun setExpand(isExpand: Boolean) {
        mIsExpand = isExpand
        requestLayout()
    }

    @AnyThread
    fun setSwitcher(switcher: View) {
        mSwitcher = switcher
    }

    @AnyThread
    fun setInterpolator(interpolator: Interpolator) {
        this.setExpandInterpolator(interpolator)
        this.setCollapseInterpolator(interpolator)
    }

    @AnyThread
    fun setExpandInterpolator(expandInterpolator: Interpolator) {
        mExpandInterpolator = expandInterpolator
    }

    @AnyThread
    fun setCollapseInterpolator(collapseInterpolator: Interpolator) {
        mCollapseInterpolator = collapseInterpolator
    }

    @AnyThread
    private fun setExecuting(isExecuting: Boolean) {
        mIsExecuting = isExecuting
    }

    //
    //  Events
    //

    @MainThread
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measureDimension(measureChildWidth(), widthMeasureSpec)
        val height = measureDimension(measureChildHeight(), heightMeasureSpec)
        mWidth = Math.max(mWidth, width)
        mHeight = Math.max(mHeight, height)
        setMeasuredDimension(width, height)
    }

    @MainThread
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (!changed) return

        if (!mIsInited) {
            visibility = if (mIsExpand) View.VISIBLE else View.GONE
            mIsInited = true
        }
    }

    //
    //  Listener
    //

    interface OnStateChangedListener {
        @MainThread
        fun onPreExpand()

        @MainThread
        fun onPreCollapse()

        @MainThread
        fun onExpanded()

        @MainThread
        fun onCollapsed()
    }

    companion object {

        private val DEFAULT_DURATION = 300
    }
}

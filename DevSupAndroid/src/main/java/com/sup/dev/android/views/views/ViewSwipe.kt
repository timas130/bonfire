package com.sup.dev.android.views.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java.tools.ToolsThreads

open class ViewSwipe constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs), View.OnTouchListener {

    var onClick: (ClickEvent) -> Unit = { performClick() }
    var onLongClick: (ClickEvent) -> Unit = { performLongClick() }
    var onSwipe: () -> Unit = { }

    private val vContainer = FrameLayout(context)
    private val vIconForAlphaAnimation: ImageView = ToolsView.inflate(this, R.layout.view_swipe_icon)
    private var colorDefault = 0

    private val maxOffset = ToolsView.dpToPx(48)
    private val longClickTime = 300L

    private val colorFocus = ToolsResources.getColor(R.color.focus)
    private val colorFocusAlpha = ToolsColor.alpha(colorFocus).toFloat()
    private val alphaAnimationTime = 500f
    private val alphaAnimationStep = 10f

    var swipeEnabled = true
    private var swiped = false
    private var swipeStarted = false
    private var startX = -1f
    private var firstX = -1f
    private var firstY = -1f
    private var firstClickTime = -1L
    private var lastX = -1f
    private var lastY = -1f
    private var focusAlpha = 0f

    private var subscriptionBack: Subscription? = null
    private var subscriptionFocus: Subscription? = null
    private var subscriptionLongClick: Subscription? = null
    private var inited = false

    init {
        colorDefault = if (background is ColorDrawable) (background as ColorDrawable).color else Color.WHITE

        val a = context.obtainStyledAttributes(attrs, R.styleable.ViewSwipe, 0, 0)
        val src = a.getResourceId(R.styleable.ViewSwipe_android_src, 0)
        val background = a.getColor(R.styleable.ViewSwipe_ViewSwipe_background, Color.TRANSPARENT)
        a.recycle()

        if (src != 0) setIcon(src)
        setBackgroundColor(background)

        addView(vIconForAlphaAnimation, 0)
        addView(vContainer, 1)

        vIconForAlphaAnimation.alpha = 0f
        (vIconForAlphaAnimation.layoutParams as LayoutParams).gravity = Gravity.CENTER or Gravity.RIGHT
        (vIconForAlphaAnimation.layoutParams as LayoutParams).rightMargin = ToolsView.dpToPx(12).toInt()
        (vIconForAlphaAnimation.layoutParams as LayoutParams).leftMargin = ToolsView.dpToPx(12).toInt()

        vContainer.setOnTouchListener(this)
        vContainer.setBackgroundColor(colorDefault)
        (vContainer.layoutParams as LayoutParams).width = LayoutParams.MATCH_PARENT
        (vContainer.layoutParams as LayoutParams).height = LayoutParams.WRAP_CONTENT

        inited = true
    }

    fun setDefaultColor(color:Int){
        colorDefault = color
        clear()
    }

    override fun requestLayout() {
        super.requestLayout()
        if (inited && childCount == 3 && vContainer.childCount == 0) {
            val vChild = getChildAt(2)
            removeView(vChild)
            vContainer.addView(vChild)
        }
    }

    fun setIcon(icon: Int) {
        vIconForAlphaAnimation.setImageResource(icon)
    }

    override fun onTouch(view: View, e: MotionEvent): Boolean {
        if (subscriptionBack != null) subscriptionBack!!.unsubscribe()
        if (subscriptionLongClick != null && (e.action != MotionEvent.ACTION_MOVE || swipeStarted
                        || firstX < e.x - ToolsView.dpToPx(12)
                        || firstX > e.x + ToolsView.dpToPx(12)
                        || firstY < e.y - ToolsView.dpToPx(12)
                        || firstY > e.y + ToolsView.dpToPx(12))) subscriptionLongClick!!.unsubscribe()

        if (e.action == MotionEvent.ACTION_DOWN) {
            startX = vContainer.x
            lastX = e.x
            lastY = e.y
            firstX = e.x
            firstY = e.y
            firstClickTime = System.currentTimeMillis()

            subscriptionFocus = ToolsThreads.timerMain(alphaAnimationStep.toLong(), alphaAnimationTime.toLong(), {
                focusAlpha += colorFocusAlpha / alphaAnimationTime * alphaAnimationStep
                focusAlpha = ToolsMath.min(focusAlpha, colorFocusAlpha)
                vContainer.setBackgroundColor(ToolsColor.add(colorDefault, ToolsColor.setAlpha(focusAlpha.toInt(), colorFocus)))
            }, {
                focusAlpha = colorFocusAlpha
                vContainer.setBackgroundColor(ToolsColor.add(colorDefault, colorFocus))
            })

            subscriptionLongClick = ToolsThreads.main(longClickTime) {
                if(subscriptionLongClick != null && subscriptionLongClick!!.isSubscribed()) {
                    clear()
                    onLongClick.invoke(ClickEvent(this, e.x, e.y))
                }
            }
            return true
        }
        if (e.action == MotionEvent.ACTION_MOVE && lastX > -1 && swipeEnabled) {
            val mx = e.x - (startX - vContainer.x)

            if (!swipeStarted && firstX > e.x - ToolsView.dpToPx(12) && firstX < e.x + ToolsView.dpToPx(12) && firstY > e.y - ToolsView.dpToPx(12) && firstY < e.y + ToolsView.dpToPx(12)) {
                if (firstClickTime < System.currentTimeMillis() - longClickTime) {
                    clear()
                    onLongClick.invoke(ClickEvent(this, e.x, e.y))
                    return true
                }
                lastX = e.x
                lastY = e.y
                return false
            } else {
                this.vContainer.requestDisallowInterceptTouchEvent(true)
                swipeStarted = true
            }

            vContainer.x = vContainer.x - (lastX - mx)
            if (vContainer.x < startX - maxOffset) {
                vContainer.x = startX - maxOffset
                swiped = true
            } else if (vContainer.x > startX + maxOffset) {
                vContainer.x = startX + maxOffset
                swiped = true
            } else {
                swiped = false
            }

            updateIcon()

            lastX = mx
            lastY = e.y
            return true
        }
        if (e.action == MotionEvent.ACTION_UP && firstX > e.x - ToolsView.dpToPx(12) && firstX < e.x + ToolsView.dpToPx(12) && firstY > e.y - ToolsView.dpToPx(12) && firstY < e.y + ToolsView.dpToPx(12)) {
            onClick.invoke(ClickEvent(this, e.x, e.y))
            clear()
            return true
        }
        if (e.action == MotionEvent.ACTION_CANCEL || e.action == MotionEvent.ACTION_UP) {
            clear()
            return true
        }
        return false
    }

    private fun updateIcon() {
        if (vIconForAlphaAnimation.layoutParams is LayoutParams) {
            (vIconForAlphaAnimation.layoutParams as LayoutParams).gravity = Gravity.CENTER or (if (vContainer.x > 0) Gravity.LEFT else Gravity.RIGHT)
            vIconForAlphaAnimation.requestLayout()
        }

        if (vContainer.x > 0) vIconForAlphaAnimation.alpha = (vContainer.x - startX) / maxOffset
        else vIconForAlphaAnimation.alpha = (startX - vContainer.x) / maxOffset

    }

    private fun clear() {
        startX = -1f
        lastX = -1f
        lastY = -1f
        firstX = -1f
        firstY = -1f
        firstClickTime = -1
        swipeStarted = false
        if (subscriptionBack != null) subscriptionBack!!.unsubscribe()
        if (subscriptionFocus != null) subscriptionFocus!!.unsubscribe()
        if (subscriptionLongClick != null) subscriptionLongClick!!.unsubscribe()

        val stepTime = 10L
        val animationTime = 150L
        val step = -vContainer.x / (animationTime / stepTime.toFloat())
        subscriptionBack = ToolsThreads.timerMain(stepTime, animationTime, {
            vContainer.x += step
            if (step > 0 && vContainer.x > 0) vContainer.x = 0f
            if (step < 0 && vContainer.x < 0) vContainer.x = 0f
            updateIcon()
        }, {
            vContainer.x = 0f
            vIconForAlphaAnimation.alpha = 0f
            if (swiped) {
                onSwipe.invoke()
                swiped = false
            }
        })
        subscriptionFocus = ToolsThreads.timerMain(alphaAnimationStep.toLong(), alphaAnimationTime.toLong(), {
            focusAlpha -= colorFocusAlpha / alphaAnimationTime * alphaAnimationStep
            focusAlpha = ToolsMath.max(focusAlpha, 0f)
            vContainer.setBackgroundColor(ToolsColor.add(colorDefault, ToolsColor.setAlpha(focusAlpha.toInt(), colorFocus)))
        }, {
            focusAlpha = 0f
            vContainer.setBackgroundColor(colorDefault)
        })
    }

    class ClickEvent{

        val x:Float
        val y:Float

        constructor(view:View, x:Float, y:Float)  {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            if(location[0] < x && location[1] < y) {
                this.x = x - location[0]
                this.y = y - location[1]
            } else {
                this.x = x
                this.y = y
            }
        }

    }



}
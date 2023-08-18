package com.sup.dev.android.views.views.layouts

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.classes.geometry.Line
import com.sup.dev.java.classes.geometry.Point
import com.sup.dev.java.classes.items.RangeF
import com.sup.dev.java.tools.ToolsThreads

class LayoutZoom constructor(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val range = RangeF(1f, 4f)

    private var onZoom: (() -> Unit)? = null
    private var animateTimeMs = 300
    var boundsView: View? = null
        get() = if (field == null) if (childCount == 0) this else getChildAt(0) else field

    override fun addView(child: View?) {
        super.addView(child)
        updateParams()
    }


    //
    //  Getters
    //

    var zoom = 1.0f
    var translateX = 0f
    var translateY = 0f
    private var subscriptionAnimateZoom: Subscription? = null

    //
    //  Click
    //

    private val click = Point()
    private var onClick: OnClickListener? = null
    private var lastDownTime = 0L
    private var clickRadius = ToolsView.dpToPx(24f)

    //
    //  Double Touch
    //

    var doubleTouchEnabled = true
    private var doubleTouchRadius = ToolsView.dpToPx(24f)
    private val doubleTouch = Point()
    private var doubleTouchTime = 0L

    //
    //  Move
    //

    private val move = Point()

    //
    //  Zoom
    //

    private val zoomLine = Line()
    private var mid = Point()

    constructor(context: Context) : this(context, null)

    init {

        SupAndroid.initEditMode(this)

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.LayoutZoom, 0, 0)
        range.min = a.getFloat(R.styleable.LayoutZoom_LayoutZoom_minZoom, range.min)
        range.max = a.getFloat(R.styleable.LayoutZoom_LayoutZoom_maxZoom, range.max)
        doubleTouchRadius = a.getDimension(R.styleable.LayoutZoom_LayoutZoom_doubleTouchRadius, doubleTouchRadius)
        animateTimeMs = a.getInteger(R.styleable.LayoutZoom_LayoutZoom_animateTimeMs, animateTimeMs)
        a.recycle()

        zoom = range.min
        updateParams()
    }

    //
    //  Events
    //

    private var onTouchEventPassed = false

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        onTouchEventPassed = false
        super.dispatchTouchEvent(ev)
        if (!onTouchEventPassed) onTouchEvent(ev)
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        onTouchEventPassed = true

        if (event.pointerCount == 1 && event.action == MotionEvent.ACTION_DOWN) {
            click(event.x, event.y)
            doubleTouch(event.x, event.y)
        }

        if (event.pointerCount == 1 && event.action == MotionEvent.ACTION_UP) {
            click(event.x, event.y)
        }

        if (event.pointerCount > 1){
            clickClear()
        }

        if (event.pointerCount == 1 && (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN)) {
            move(event.x, event.y)
        } else {
            clearMove()
        }

        if (event.pointerCount == 2 && (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_POINTER_DOWN)) {
            zoom(Line(event.x, event.y, event.getX(1), event.getY(1)))
        } else {
            clearZoomParams()
        }

        return true
    }

    private fun doubleTouch(x: Float, y: Float) {

        if (!doubleTouchEnabled) return

        if (doubleTouchTime > System.currentTimeMillis() - 300 && doubleTouch.inRadius(x, y, doubleTouchRadius)) {
            animateZoom(if (zoom == 1f) (range.max - range.min) / 2 + range.min else 1F, x, y)
            doubleTouch.clear()
            doubleTouchTime = 0
            clickClear()
        } else {
            doubleTouch.set(x, y)
            doubleTouchTime = System.currentTimeMillis()
        }
    }

    private fun click(x: Float, y: Float) {

        if (onClick == null) return

        if (lastDownTime != 0L  && click.inRadius(x, y, clickRadius)) {
            click.clear()
            val key = lastDownTime
            ToolsThreads.main(300) {
                if (lastDownTime == key) {
                    onClick?.onClick(this)
                    lastDownTime = 0L
                }
            }
        } else {
            click.set(x, y)
            lastDownTime = System.currentTimeMillis()
        }
    }

    private fun clickClear() {
        click.clear()
        lastDownTime = 0L
    }

    override fun setOnClickListener(l: OnClickListener?) {
        this.onClick = l
    }

    fun move(x: Float, y: Float) {

        if (move.isEmpty()) move.set(x, y)

        translateX += x - move.x
        translateY += y - move.y

        updateParams()

        move.set(x, y)
    }

    private fun clearMove() {
        move.clear()
    }

    private fun zoom(touchLine: Line) {

        if (zoomLine.isEmpty()) {
            zoomLine.set(touchLine)
            mid = touchLine.middle()
        }

        val zoomChange = (touchLine.length() / zoomLine.length() - 1) * zoom

        zoom(zoomChange, mid.x, mid.y)
        zoomLine.set(touchLine)
    }

    private fun clearZoomParams() {
        zoomLine.clear()
        mid.clear()
    }

    fun zoom(zoomChange: Float, midX: Float, midY: Float) {
        var zoomChangeV = zoomChange
        zoom += zoomChangeV

        if (zoom < range.min) {
            zoomChangeV += range.min - zoom
            zoom = range.min
        }
        if (zoom > range.max) {
            zoomChangeV -= zoom - range.max
            zoom = range.max
        }

        translateX += (width / 2 - midX) * zoomChangeV
        translateY += (height / 2 - midY) * zoomChangeV

        updateParams()
    }

/*

    public void animateZoom(float targetZoom, float midX, float midY) {

        subscriptionAnimateZoom = ToolsThreads.timerMain(animateTimeMs/fameCount, animateTimeMs+100,
                subscription -> zoom(step, midX, midY));

    }
 */

    fun animateZoom(targetZoom: Float, midX: Float, midY: Float) {

        if (subscriptionAnimateZoom != null) subscriptionAnimateZoom!!.unsubscribe()

        val fameCount = animateTimeMs * 60 / 1000
        val step = (targetZoom - zoom) / fameCount

        subscriptionAnimateZoom = ToolsThreads.timerMain(animateTimeMs / fameCount.toLong(), animateTimeMs + 100.toLong(), { zoom(step, midX, midY) })

    }

    fun reset() {
        zoom = 1f
        translateX = 0f
        translateY = 0f
        updateParams()
    }

    //
    //  Params
    //

    fun updateParams() {

        parent?.requestDisallowInterceptTouchEvent(zoom > 1.2)

        val vBound = boundsView

        if (width > vBound!!.width * zoom)
            translateX = 0f
        else
            translateX = RangeF((width - vBound.width * zoom) / 2).toRange(translateX)

        if (height > vBound.height * zoom)
            translateY = 0f
        else
            translateY = RangeF((height - vBound.height * zoom) / 2).toRange(translateY)

        for (i in 0 until childCount) {
            val v = getChildAt(i)
            v.scaleX = zoom
            v.scaleY = zoom
            v.translationX = translateX
            v.translationY = translateY
        }

        if (onZoom != null) onZoom!!.invoke()
    }

    //
    //  State
    //

    public override fun onSaveInstanceState(): Parcelable? {

        val bundle = Bundle()
        bundle.putParcelable("SUPER_STATE", super.onSaveInstanceState())
        bundle.putFloat("range_min", range.min)
        bundle.putFloat("range_max", range.max)
        bundle.putInt("animateTimeMs", animateTimeMs)
        bundle.putFloat("doubleTouchRadius", doubleTouchRadius)
        bundle.putFloat("zoom", zoom)
        bundle.putFloat("translateX", translateX)
        bundle.putFloat("translateY", translateY)
        bundle.putFloat("w", width.toFloat())
        bundle.putFloat("h", height.toFloat())

        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable?) {
        var stateV = state
        if (stateV is Bundle) {
            val bundle = stateV as Bundle?
            range.min = bundle!!.getFloat("range_min")
            range.max = bundle.getFloat("range_max")
            animateTimeMs = bundle.getInt("animateTimeMs")
            doubleTouchRadius = bundle.getFloat("doubleTouchRadius")
            zoom = bundle.getFloat("zoom")
            translateX = bundle.getFloat("translateX")
            translateY = bundle.getFloat("translateY")
            val w = bundle.getFloat("w")
            val h = bundle.getFloat("h")

            stateV = bundle.getParcelable("SUPER_STATE")

            ToolsThreads.main(true) {
                if (w != 0f && h != 0f) {
                    translateX *= if (width > w) width / w else w / width
                    translateY *= if (height > h) height / h else h / height
                    updateParams()
                }
            }
        }
        super.onRestoreInstanceState(stateV)
    }

    //
    //  Setters
    //

    fun setOnZoom(onZoom: () -> Unit) {
        this.onZoom = onZoom
    }

    fun setMaxZoom(maxZoom: Float) {
        range.max = maxZoom
        if (zoom > maxZoom) {
            zoom = maxZoom
            updateParams()
        }
    }

    fun setMinZoom(minZoom: Float) {
        range.min = minZoom
        if (zoom < minZoom) {
            zoom = minZoom
            updateParams()
        }
    }

    fun setAnimateTimeMs(animateTimeMs: Int) {
        this.animateTimeMs = animateTimeMs
    }

    fun setDoubleTouchRadius(doubleTouchRadiusDp: Int) {
        this.doubleTouchRadius = ToolsView.dpToPx(doubleTouchRadiusDp)
    }
}
